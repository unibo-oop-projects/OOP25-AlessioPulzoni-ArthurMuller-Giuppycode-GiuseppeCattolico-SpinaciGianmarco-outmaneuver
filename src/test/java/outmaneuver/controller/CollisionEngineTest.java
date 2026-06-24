package outmaneuver.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.controller.event.InternalEventListener;
import outmaneuver.controller.event.CollisionEvent;
import outmaneuver.controller.event.Event;
import outmaneuver.model.area.collision.CollisionData;
import outmaneuver.model.area.entity.missile.MissileImpl;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.util.Vector2;

class CollisionEngineTest {

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

    private RecordingListener listener;
    private CollisionEngine engine;

    @BeforeEach
    void setUp() {
        listener = new RecordingListener();
        engine = new CollisionEngine(listener);
    }

    @Test
    void overlappingMissilesTriggerCollisionEvent() {
        final MissileImpl a = new TestMissile(new Vector2(0, 0));
        final MissileImpl b = new TestMissile(new Vector2(5, 0));

        engine.register(a);
        engine.register(b);
        engine.tick();

        assertTrue(listener.events.contains(CollisionEvent.MISSILE_MISSILE_COLLISION),
                "Overlapping missiles should trigger a missile-missile collision event");

        final CollisionData data = (CollisionData) payloadFor(CollisionEvent.MISSILE_MISSILE_COLLISION);
        assertTrue((data.getEntityA() == a && data.getEntityB() == b)
                || (data.getEntityA() == b && data.getEntityB() == a));
    }

    @Test
    void distantMissilesDoNotTriggerCollisionEvent() {
        final MissileImpl a = new TestMissile(new Vector2(0, 0));
        final MissileImpl b = new TestMissile(new Vector2(1000, 1000));

        engine.register(a);
        engine.register(b);
        engine.tick();

        assertTrue(listener.events.isEmpty(), "Distant missiles should not trigger a collision event");
    }

    @Test
    void unregisteredMissileDoesNotTriggerCollisionEvent() {
        final MissileImpl a = new TestMissile(new Vector2(0, 0));
        final MissileImpl b = new TestMissile(new Vector2(5, 0));

        engine.register(a);
        engine.register(b);
        engine.unregister(b);
        engine.tick();

        assertTrue(listener.events.isEmpty(), "Unregistered entities should not participate in collisions");
    }

    @Test
    void clearAllRemovesAllEntities() {
        final MissileImpl a = new TestMissile(new Vector2(0, 0));
        final MissileImpl b = new TestMissile(new Vector2(5, 0));

        engine.register(a);
        engine.register(b);
        engine.clearAll();
        engine.tick();

        assertTrue(listener.events.isEmpty(), "clearAll should remove all registered entities");
    }

    @Test
    void sameMissileDoesNotCollideWithItself() {
        final MissileImpl a = new TestMissile(new Vector2(0, 0));

        engine.register(a);
        engine.tick();

        assertTrue(listener.events.isEmpty(), "A single entity must not collide with itself");
    }

    private Object payloadFor(final CollisionEvent event) {
        final int index = listener.events.indexOf(event);
        assertTrue(index >= 0);
        return listener.payloads.get(index);
    }
}
