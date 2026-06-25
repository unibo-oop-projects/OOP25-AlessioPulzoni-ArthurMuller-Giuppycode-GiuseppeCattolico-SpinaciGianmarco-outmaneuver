package outmaneuver.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.controller.event.CollisionEvent;
import outmaneuver.controller.event.Event;
import outmaneuver.controller.event.InternalEventListener;
import outmaneuver.controller.impl.EntityControllerImpl;
import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.missile.MissileImpl;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.model.area.entity.plane.PlaneData;
import outmaneuver.model.area.entity.plane.PlaneImpl;
import outmaneuver.util.Vector2;
import outmaneuver.view.GameView;
import outmaneuver.view.RenderState;

class EntityControllerImplTest {

    // ── Fixtures ─────────────────────────────────────────────────────

    private PlaneImpl plane;
    private ConcreteEntityController entityCtrl;

    // ── Test doubles ─────────────────────────────────────────────────

    /** Bare concrete subclass: exercises the base-class behaviour with no overrides. */
    private static final class ConcreteEntityController extends EntityControllerImpl {
        ConcreteEntityController(final List<Entity> entities, final CollisionEngine collisionEngine) {
            super(entities, collisionEngine);
        }

        GameView exposeView() {
            return getView();
        }
    }

    /** Missile concreto minimale per i test: raggio 8 cosi' due missili vicini collidono. */
    private static final class TestMissile extends MissileImpl {
        TestMissile(final Vector2 pos) {
            super(pos, new MissileData("test", 1.0, 0.0, 8.0, -1.0, 0.0, 0, null));
        }
    }

    private static final class RecordingListener implements InternalEventListener {
        final List<Event> events = new ArrayList<>();
        final List<Object> payloads = new ArrayList<>();

        @Override
        public void onInternalEvent(final Event evt, final Object data) {
            events.add(evt);
            payloads.add(data);
        }
    }

    private static final class StubGameView implements GameView {
        @Override public void renderFrame(final RenderState state) { }
        @Override public int getWidth() { return 800; }
        @Override public int getHeight() { return 600; }
    }

    @BeforeEach
    void setUp() {
        plane = new PlaneImpl(new PlaneData("standard", 200, 3, 20, "aircraft_standard", 0));
        final CollisionEngine collisionEngine = new CollisionEngine(new RecordingListener());
        entityCtrl = new ConcreteEntityController(new ArrayList<>(), collisionEngine);
    }

    // ── spawnEntity ──────────────────────────────────────────────────

    @Test
    void spawnEntityAddsToEntities() {
        entityCtrl.spawnEntity(plane);
        assertTrue(entityCtrl.getEntities().contains(plane));
    }

    @Test
    void spawnEntityRegistersWithCollisionEngine() {
        final RecordingListener listener = new RecordingListener();
        final CollisionEngine engine = new CollisionEngine(listener);
        final ConcreteEntityController ctrl = new ConcreteEntityController(new ArrayList<>(), engine);

        final MissileImpl a = new TestMissile(new Vector2(0, 0));
        final MissileImpl b = new TestMissile(new Vector2(5, 0));
        ctrl.spawnEntity(a);
        ctrl.spawnEntity(b);
        engine.tick();

        assertTrue(listener.events.contains(CollisionEvent.MISSILE_MISSILE_COLLISION),
                "Entities spawned through the controller should be registered with the collision engine");
    }

    @Test
    void spawnEntityNullThrows() {
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class,
                () -> entityCtrl.spawnEntity(null));
    }

    // ── removeEntity ─────────────────────────────────────────────────

    @Test
    void removeEntityRemovesFromEntities() {
        entityCtrl.spawnEntity(plane);
        entityCtrl.removeEntity(plane);
        assertFalse(entityCtrl.getEntities().contains(plane));
    }

    @Test
    void removeEntityUnregistersFromCollisionEngine() {
        final RecordingListener listener = new RecordingListener();
        final CollisionEngine engine = new CollisionEngine(listener);
        final ConcreteEntityController ctrl = new ConcreteEntityController(new ArrayList<>(), engine);

        final MissileImpl a = new TestMissile(new Vector2(0, 0));
        final MissileImpl b = new TestMissile(new Vector2(5, 0));
        ctrl.spawnEntity(a);
        ctrl.spawnEntity(b);
        ctrl.removeEntity(b);
        engine.tick();

        assertTrue(listener.events.isEmpty(), "Removed entity should no longer participate in collisions");
    }

    // ── removeAll ────────────────────────────────────────────────────

    @Test
    void removeAllClearsEntitiesAndUnregistersFromCollisionEngine() {
        final RecordingListener listener = new RecordingListener();
        final CollisionEngine engine = new CollisionEngine(listener);
        final ConcreteEntityController ctrl = new ConcreteEntityController(new ArrayList<>(), engine);

        final MissileImpl a = new TestMissile(new Vector2(0, 0));
        final MissileImpl b = new TestMissile(new Vector2(5, 0));
        ctrl.spawnEntity(a);
        ctrl.spawnEntity(b);

        ctrl.removeAll();
        engine.tick();

        assertTrue(ctrl.getEntities().isEmpty(), "removeAll should leave no entities behind");
        assertTrue(listener.events.isEmpty(), "Entities removed via removeAll should no longer collide");
    }

    // ── clearAll (default no-op, overridable by subclasses) ───────────

    @Test
    void clearAllIsNoOpByDefault() {
        entityCtrl.spawnEntity(plane);
        entityCtrl.clearAll();
        assertTrue(entityCtrl.getEntities().contains(plane),
                "The base EntityControllerImpl.clearAll() must not touch the entity list");
    }

    // ── getEntities ──────────────────────────────────────────────────

    @Test
    void entitiesReturnsSnapshotNotBackedByInternalList() {
        entityCtrl.spawnEntity(plane);
        final List<Entity> snapshot = entityCtrl.getEntities();
        entityCtrl.spawnEntity(new TestMissile(Vector2.ZERO));

        assertEquals(1, snapshot.size(), "Previously returned snapshot must not reflect later mutations");
    }

    // ── onInternalEvent ──────────────────────────────────────────────

    @Test
    void onInternalEventForwardsToRegisteredListener() {
        final RecordingListener listener = new RecordingListener();
        entityCtrl.setEventListener(listener);

        final Object payload = new Object();
        entityCtrl.onInternalEvent(CollisionEvent.MISSILE_MISSILE_COLLISION, payload);

        assertEquals(List.of(CollisionEvent.MISSILE_MISSILE_COLLISION), listener.events);
        assertEquals(List.of(payload), listener.payloads);
    }

    @Test
    void onInternalEventWithoutListenerDoesNotThrow() {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> entityCtrl.onInternalEvent(CollisionEvent.MISSILE_MISSILE_COLLISION, null));
    }

    // ── setView/getView ──────────────────────────────────────────────

    @Test
    void setViewIsVisibleToSubclassesViaGetView() {
        final StubGameView view = new StubGameView();
        entityCtrl.setView(view);
        assertEquals(view, entityCtrl.exposeView());
    }
}
