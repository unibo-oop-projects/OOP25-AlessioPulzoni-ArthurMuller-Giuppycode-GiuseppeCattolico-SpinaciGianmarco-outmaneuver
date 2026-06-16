package outmaneuver.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.controller.event.InternalEventListener;
import outmaneuver.controller.impl.EntityControllerImpl;
import outmaneuver.controller.impl.InputControllerImpl;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.area.entity.plane.PlaneData;
import outmaneuver.model.area.entity.plane.PlaneImpl;
import outmaneuver.model.area.entity.plane.TurnState;
import outmaneuver.model.area.entity.collectibles.AbstractCollectible;
import outmaneuver.model.area.entity.missile.MissileImpl;
import outmaneuver.model.session.GameState;
import outmaneuver.model.session.IGameSession;
import outmaneuver.util.Vector2;

class EntityControllerImplTest {

    private static final double EPS = 1e-9;

    // ── Test doubles ─────────────────────────────────────────────────

    private static class RecordingListener implements InternalEventListener {
        final List<InternalEvent> events = new ArrayList<>();

        @Override
        public void onInternalEvent(final InternalEvent evt, final Object data) {
            events.add(evt);
        }
    }

    private static class StubCollectible extends AbstractCollectible {
        boolean applied = false;

        StubCollectible(final Vector2 position) {
            super(position);
        }

        @Override
        public void apply(final Plane plane, final IGameSession session) {
            applied = true;
        }
    }

    private static final IGameSession NO_OP_SESSION = new IGameSession() {
        @Override public GameState getGameState() { return null; }
        @Override public int getScore() { return 0; }
        @Override public long getElapsedTimeMillis() { return 0; }
        @Override public void incrementScore(final int delta) { }
        @Override public void transitionTo(final GameState state) { }
        @Override public void reset() { }
    };

    // ── Fixtures ─────────────────────────────────────────────────────

    private PlaneImpl plane;
    private InputControllerImpl input;
    private RecordingListener listener;
    private CollisionEngine collisionEngine;
    private EntityControllerImpl entityCtrl;

    @BeforeEach
    void setUp() {
        plane = new PlaneImpl(new PlaneData("standard", 200, 3, 20, "aircraft_standard", 0));
        input = new InputControllerImpl();
        listener = new RecordingListener();
        collisionEngine = new CollisionEngine(listener);
        entityCtrl = new EntityControllerImpl(input, listener, collisionEngine, NO_OP_SESSION);
    }

    // ── spawnPlane ───────────────────────────────────────────────────

    @Test
    void spawnPlane_addsToEntities() {
        entityCtrl.spawnPlane(plane);
        assertTrue(entityCtrl.getEntities().contains(plane));
    }

    // ── spawnMissile ─────────────────────────────────────────────────

    @Test
    void spawnMissile_addsToEntities() {
        final MissileImpl missile = new MissileImpl(Vector2.ZERO, 0, 100);
        entityCtrl.spawnMissile(missile);
        assertTrue(entityCtrl.getEntities().contains(missile));
    }

    // ── spawnCollectible ─────────────────────────────────────────────

    @Test
    void spawnCollectible_addsToEntities() {
        final StubCollectible col = new StubCollectible(new Vector2(500, 500));
        entityCtrl.spawnCollectible(col);
        assertTrue(entityCtrl.getEntities().contains(col));
    }

    // ── updateEntities – plane movement ──────────────────────────────

    @Test
    void noInput_doesNotChangeDirection() {
        entityCtrl.spawnPlane(plane);
        final double initialDir = plane.getDirection();
        entityCtrl.updateEntities(16);
        assertEquals(initialDir, plane.getDirection(), EPS);
    }

    @Test
    void noInput_planeStillMovesForward() {
        entityCtrl.spawnPlane(plane);
        plane.setDirection(0);
        final Vector2 before = plane.getPosition();
        entityCtrl.updateEntities(100);
        assertNotEquals(before, plane.getPosition());
    }

    @Test
    void leftInput_rotatesLeft() {
        entityCtrl.spawnPlane(plane);
        input.onKeyPressed(37); // VK_LEFT
        entityCtrl.updateEntities(100);
        assertTrue(plane.getDirection() < 0, "Direction should be negative after left turn");
    }

    @Test
    void rightInput_rotatesRight() {
        entityCtrl.spawnPlane(plane);
        input.onKeyPressed(39); // VK_RIGHT
        entityCtrl.updateEntities(100);
        assertTrue(plane.getDirection() > 0, "Direction should be positive after right turn");
    }

    @Test
    void leftInput_setsTurnStateLeft() {
        entityCtrl.spawnPlane(plane);
        input.onKeyPressed(37);
        entityCtrl.updateEntities(16);
        assertEquals(TurnState.LEFT, plane.getTurnState());
    }

    @Test
    void rightInput_setsTurnStateRight() {
        entityCtrl.spawnPlane(plane);
        input.onKeyPressed(39);
        entityCtrl.updateEntities(16);
        assertEquals(TurnState.RIGHT, plane.getTurnState());
    }

    @Test
    void noInput_setsTurnStateNone() {
        entityCtrl.spawnPlane(plane);
        entityCtrl.updateEntities(16);
        assertEquals(TurnState.NONE, plane.getTurnState());
    }

    @Test
    void planeMovesForwardAlongXAxis() {
        entityCtrl.spawnPlane(plane);
        plane.setPosition(new Vector2(100, 100));
        plane.setDirection(0);
        entityCtrl.updateEntities(1000);
        assertTrue(plane.getPosition().getX() > 100, "Plane should advance in +X");
        assertEquals(100, plane.getPosition().getY(), EPS, "Y should stay unchanged");
    }

    @Test
    void directionStaysWithinMinusPiToPi() {
        entityCtrl.spawnPlane(plane);
        input.onKeyPressed(37);
        for (int i = 0; i < 1000; i++) {
            entityCtrl.updateEntities(100);
        }
        final double dir = plane.getDirection();
        assertTrue(dir >= -Math.PI - EPS && dir <= Math.PI + EPS,
                "Direction out of [-PI, PI]: " + dir);
    }

    // ── updateEntities – missile movement ────────────────────────────

    @Test
    void missileUpdateAdvancesPosition() {
        final MissileImpl missile = new MissileImpl(Vector2.ZERO, 0, 200);
        entityCtrl.spawnMissile(missile);
        entityCtrl.updateEntities(1000);
        assertTrue(missile.getPosition().getX() > 0, "Missile should move in +X direction");
    }

    // ── collectible pickup ────────────────────────────────────────────

    @Test
    void overlappingCollectible_isRemovedAppliedAndFiresEvent() {
        entityCtrl.spawnPlane(plane);
        // Same position as plane → hitboxes overlap
        final StubCollectible col = new StubCollectible(plane.getPosition());
        entityCtrl.spawnCollectible(col);

        entityCtrl.updateEntities(16);

        assertFalse(entityCtrl.getEntities().contains(col), "Collectible should be removed on pickup");
        assertTrue(col.applied, "apply() should have been called");
        assertTrue(listener.events.contains(InternalEvent.PLANE_COLLECTIBLE_COLLISION),
                "PLANE_COLLECTIBLE_COLLISION event should be fired");
    }

    @Test
    void distantCollectible_isNotPickedUp() {
        entityCtrl.spawnPlane(plane);
        final StubCollectible col = new StubCollectible(new Vector2(99999, 99999));
        entityCtrl.spawnCollectible(col);

        entityCtrl.updateEntities(16);

        assertTrue(entityCtrl.getEntities().contains(col), "Distant collectible should remain");
        assertFalse(col.applied, "apply() should not have been called");
    }

    // ── clearAll ──────────────────────────────────────────────────────

    @Test
    void clearAll_keepsPlanAndResetsState() {
        entityCtrl.spawnPlane(plane);
        plane.setDirection(Math.PI / 2);
        plane.setPosition(new Vector2(300, 400));

        entityCtrl.clearAll();

        assertTrue(entityCtrl.getEntities().contains(plane), "Plane should stay after clearAll");
        assertEquals(Vector2.ZERO, plane.getPosition());
        assertEquals(0, plane.getDirection(), EPS);
        assertEquals(TurnState.NONE, plane.getTurnState());
    }

    @Test
    void clearAll_removesMissiles() {
        final MissileImpl missile = new MissileImpl(Vector2.ZERO, 0, 0);
        entityCtrl.spawnMissile(missile);

        entityCtrl.clearAll();

        assertFalse(entityCtrl.getEntities().contains(missile), "Missile should be gone after clearAll");
    }

    @Test
    void clearAll_removesCollectibles() {
        final StubCollectible col = new StubCollectible(new Vector2(200, 200));
        entityCtrl.spawnCollectible(col);

        entityCtrl.clearAll();

        assertFalse(entityCtrl.getEntities().contains(col), "Collectible should be gone after clearAll");
    }

    // ── removeEntity ──────────────────────────────────────────────────

    @Test
    void removeEntity_removesCollectible() {
        final StubCollectible col = new StubCollectible(Vector2.ZERO);
        entityCtrl.spawnCollectible(col);
        entityCtrl.removeEntity(col);
        assertFalse(entityCtrl.getEntities().contains(col));
    }

    @Test
    void removeEntity_removesMissile() {
        final MissileImpl missile = new MissileImpl(Vector2.ZERO, 0, 0);
        entityCtrl.spawnMissile(missile);
        entityCtrl.removeEntity(missile);
        assertFalse(entityCtrl.getEntities().contains(missile));
    }

    @Test
    void removeEntity_doesNotRemovePlane() {
        entityCtrl.spawnPlane(plane);
        entityCtrl.removeEntity(plane);
        assertTrue(entityCtrl.getEntities().contains(plane), "Plane must never be removed");
    }
}
