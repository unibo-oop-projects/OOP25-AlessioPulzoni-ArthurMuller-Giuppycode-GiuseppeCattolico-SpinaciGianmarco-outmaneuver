package outmaneuver.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.controller.event.InternalEventListener;
import outmaneuver.controller.event.CollisionEvent;
import outmaneuver.controller.event.Event;
import outmaneuver.controller.impl.EntityControllerImpl;
import outmaneuver.model.area.collision.CollisionData;
import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.collectibles.AbstractCollectible;
import outmaneuver.model.area.entity.missile.MissileImpl;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.area.entity.plane.PlaneData;
import outmaneuver.model.area.entity.plane.PlaneImpl;
import outmaneuver.model.area.entity.plane.TurnState;
import outmaneuver.model.session.GameState;
import outmaneuver.model.session.IGameSession;
import outmaneuver.util.Vector2;

class EntityControllerImplTest {

    // ── Test doubles ─────────────────────────────────────────────────

    private static final class ConcreteEntityController extends EntityControllerImpl {
        ConcreteEntityController(final List<Entity> entities, final CollisionEngine collisionEngine,
                                  final IGameSession session) {
            super(entities, collisionEngine, session);
        }
    }

    /** Missile concreto minimale per i test: raggio 8 così due missili vicini collidono. */
    private static final class TestMissile extends MissileImpl {
        TestMissile(final Vector2 pos) {
            super(pos, new MissileData("test", 1.0, 0.0, 8.0, -1.0, 0.0, 0, null));
        }
    }

    private static class RecordingListener implements InternalEventListener {
        final List<CollisionEvent> events = new ArrayList<>();

        @Override
        public void onInternalEvent(final Event evt, final Object data) {
            if (evt instanceof CollisionEvent) {
                events.add((CollisionEvent) evt);
            }
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

        @Override
        public String getCollectibleType() {
            return "star";
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
    private RecordingListener listener;
    private CollisionEngine collisionEngine;
    private ConcreteEntityController entityCtrl;

    @BeforeEach
    void setUp() {
        plane = new PlaneImpl(new PlaneData("standard", 200, 3, 20, "aircraft_standard", 0));
        listener = new RecordingListener();
        collisionEngine = new CollisionEngine(listener);
        entityCtrl = new ConcreteEntityController(new ArrayList<>(), collisionEngine, NO_OP_SESSION);
    }

    // ── spawnEntity ──────────────────────────────────────────────────

    @Test
    void spawnEntity_addsToEntities() {
        entityCtrl.spawnEntity(plane);
        assertTrue(entityCtrl.getEntities().contains(plane));
    }

    @Test
    void spawnEntity_missileRegistersWithCollisionEngine() {
        final MissileImpl a = new TestMissile(new Vector2(0, 0));
        final MissileImpl b = new TestMissile(new Vector2(5, 0));

        entityCtrl.spawnEntity(a);
        entityCtrl.spawnEntity(b);
        collisionEngine.tick();

        assertTrue(listener.events.contains(CollisionEvent.MISSILE_MISSILE_COLLISION),
                "Missiles spawned through the controller should be registered with the collision engine");
    }

    // ── removeEntity ─────────────────────────────────────────────────

    @Test
    void removeEntity_removesCollectible() {
        final StubCollectible col = new StubCollectible(Vector2.ZERO);
        entityCtrl.spawnEntity(col);
        entityCtrl.removeEntity(col);
        assertFalse(entityCtrl.getEntities().contains(col));
    }

    @Test
    void removeEntity_unregistersMissileFromCollisionEngine() {
        final MissileImpl a = new TestMissile(new Vector2(0, 0));
        final MissileImpl b = new TestMissile(new Vector2(5, 0));
        entityCtrl.spawnEntity(a);
        entityCtrl.spawnEntity(b);

        entityCtrl.removeEntity(b);
        collisionEngine.tick();

        assertTrue(listener.events.isEmpty(), "Removed missile should no longer participate in collisions");
    }

    @Test
    void removeEntity_doesNotRemovePlane() {
        entityCtrl.spawnEntity(plane);
        entityCtrl.removeEntity(plane);
        assertTrue(entityCtrl.getEntities().contains(plane), "Plane must never be removed");
    }

    // ── clearAll ─────────────────────────────────────────────────────

    @Test
    void clearAll_keepsPlaneAndResetsState() {
        entityCtrl.spawnEntity(plane);
        plane.setDirection(Math.PI / 2);
        plane.setPosition(new Vector2(300, 400));

        entityCtrl.clearAll();

        assertTrue(entityCtrl.getEntities().contains(plane), "Plane should stay after clearAll");
        assertEquals(Vector2.ZERO, plane.getPosition());
        assertEquals(0, plane.getDirection(), 1e-9);
        assertEquals(TurnState.NONE, plane.getTurnState());
    }

    @Test
    void clearAll_removesMissilesAndUnregistersFromCollisionEngine() {
        final MissileImpl a = new TestMissile(new Vector2(0, 0));
        final MissileImpl b = new TestMissile(new Vector2(5, 0));
        entityCtrl.spawnEntity(a);
        entityCtrl.spawnEntity(b);

        entityCtrl.clearAll();
        collisionEngine.tick();

        assertFalse(entityCtrl.getEntities().contains(a), "Missile should be gone after clearAll");
        assertTrue(listener.events.isEmpty(), "Cleared missiles should no longer participate in collisions");
    }

    @Test
    void clearAll_removesCollectibles() {
        final StubCollectible col = new StubCollectible(new Vector2(200, 200));
        entityCtrl.spawnEntity(col);

        entityCtrl.clearAll();

        assertFalse(entityCtrl.getEntities().contains(col), "Collectible should be gone after clearAll");
    }

    // ── onInternalEvent ──────────────────────────────────────────────

    @Test
    void onInternalEvent_planeMissileCollision_removesMissile() {
        final MissileImpl missile = new TestMissile(plane.getPosition());
        entityCtrl.spawnEntity(plane);
        entityCtrl.spawnEntity(missile);

        final CollisionData data = new CollisionData(missile, plane, plane.getPosition());
        entityCtrl.onInternalEvent(CollisionEvent.PLANE_MISSILE_COLLISION, data);

        assertFalse(entityCtrl.getEntities().contains(missile), "Missile should be removed on plane hit");
    }

    @Test
    void onInternalEvent_planeCollectibleCollision_appliesAndRemoves() {
        final StubCollectible col = new StubCollectible(plane.getPosition());
        entityCtrl.spawnEntity(plane);
        entityCtrl.spawnEntity(col);

        final CollisionData data = new CollisionData(plane, col, plane.getPosition());
        entityCtrl.onInternalEvent(CollisionEvent.PLANE_COLLECTIBLE_COLLISION, data);

        assertFalse(entityCtrl.getEntities().contains(col), "Collectible should be removed on pickup");
        assertTrue(col.applied, "apply() should have been called");
    }

    @Test
    void onInternalEvent_missileMissileCollision_removesBothMissiles() {
        final MissileImpl a = new TestMissile(new Vector2(0, 0));
        final MissileImpl b = new TestMissile(new Vector2(5, 0));
        entityCtrl.spawnEntity(a);
        entityCtrl.spawnEntity(b);

        final CollisionData data = new CollisionData(a, b, Vector2.ZERO);
        entityCtrl.onInternalEvent(CollisionEvent.MISSILE_MISSILE_COLLISION, data);

        assertFalse(entityCtrl.getEntities().contains(a), "First missile should be removed");
        assertFalse(entityCtrl.getEntities().contains(b), "Second missile should be removed");
    }
}
