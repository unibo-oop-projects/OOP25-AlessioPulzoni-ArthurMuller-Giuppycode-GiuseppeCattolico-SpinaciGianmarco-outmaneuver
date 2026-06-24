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

    private static class RecordingListener implements InternalEventListener {
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

    // ── Fixtures ─────────────────────────────────────────────────────

    private PlaneImpl plane;
    private CollisionEngine collisionEngine;
    private ConcreteEntityController entityCtrl;

    @BeforeEach
    void setUp() {
        plane = new PlaneImpl(new PlaneData("standard", 200, 3, 20, "aircraft_standard", 0));
        collisionEngine = new CollisionEngine(new RecordingListener());
        entityCtrl = new ConcreteEntityController(new ArrayList<>(), collisionEngine);
    }

    // ── spawnEntity ──────────────────────────────────────────────────

    @Test
    void spawnEntity_addsToEntities() {
        entityCtrl.spawnEntity(plane);
        assertTrue(entityCtrl.getEntities().contains(plane));
    }

    @Test
    void spawnEntity_registersWithCollisionEngine() {
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
    void spawnEntity_nullThrows() {
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class,
                () -> entityCtrl.spawnEntity(null));
    }

    // ── removeEntity ─────────────────────────────────────────────────

    @Test
    void removeEntity_removesFromEntities() {
        entityCtrl.spawnEntity(plane);
        entityCtrl.removeEntity(plane);
        assertFalse(entityCtrl.getEntities().contains(plane));
    }

    @Test
    void removeEntity_unregistersFromCollisionEngine() {
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
    void removeAll_clearsEntitiesAndUnregistersFromCollisionEngine() {
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
    void clearAll_isNoOpByDefault() {
        entityCtrl.spawnEntity(plane);
        entityCtrl.clearAll();
        assertTrue(entityCtrl.getEntities().contains(plane),
                "The base EntityControllerImpl.clearAll() must not touch the entity list");
    }

    // ── getEntities ──────────────────────────────────────────────────

    @Test
    void getEntities_returnsSnapshotNotBackedByInternalList() {
        entityCtrl.spawnEntity(plane);
        final List<Entity> snapshot = entityCtrl.getEntities();
        entityCtrl.spawnEntity(new TestMissile(Vector2.ZERO));

        assertEquals(1, snapshot.size(), "Previously returned snapshot must not reflect later mutations");
    }

    // ── onInternalEvent ──────────────────────────────────────────────

    @Test
    void onInternalEvent_forwardsToRegisteredListener() {
        final RecordingListener listener = new RecordingListener();
        entityCtrl.setEventListener(listener);

        final Object payload = new Object();
        entityCtrl.onInternalEvent(CollisionEvent.MISSILE_MISSILE_COLLISION, payload);

        assertEquals(List.of(CollisionEvent.MISSILE_MISSILE_COLLISION), listener.events);
        assertEquals(List.of(payload), listener.payloads);
    }

    @Test
    void onInternalEvent_withoutListenerDoesNotThrow() {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> entityCtrl.onInternalEvent(CollisionEvent.MISSILE_MISSILE_COLLISION, null));
    }

    // ── setView/getView ──────────────────────────────────────────────

    @Test
    void setView_isVisibleToSubclassesViaGetView() {
        final StubGameView view = new StubGameView();
        entityCtrl.setView(view);
        assertEquals(view, entityCtrl.exposeView());
    }
}
