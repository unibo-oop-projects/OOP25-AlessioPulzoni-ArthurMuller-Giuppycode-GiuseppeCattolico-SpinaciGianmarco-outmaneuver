package outmaneuver.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.controller.event.InternalEventListener;
import outmaneuver.model.area.entity.missile.MissileImpl;
import outmaneuver.model.collision.CollisionData;
import outmaneuver.util.Vector2;

class CollisionEngineTest {

    private static class RecordingListener implements InternalEventListener {
        final List<InternalEvent> events = new ArrayList<>();
        final List<Object> payloads = new ArrayList<>();

        @Override
        public void onInternalEvent(final InternalEvent evt, final Object data) {
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
        final MissileImpl a = new MissileImpl(new Vector2(0, 0), 0, 0);
        final MissileImpl b = new MissileImpl(new Vector2(5, 0), 0, 0);

        engine.register(a);
        engine.register(b);
        engine.tick();

        assertTrue(listener.events.contains(InternalEvent.MISSILE_MISSILE_COLLISION),
                "Overlapping missiles should trigger a missile-missile collision event");

        final CollisionData data = (CollisionData) payloadFor(InternalEvent.MISSILE_MISSILE_COLLISION);
        assertTrue((data.getEntityA() == a && data.getEntityB() == b)
                || (data.getEntityA() == b && data.getEntityB() == a));
    }

    @Test
    void distantMissilesDoNotTriggerCollisionEvent() {
        final MissileImpl a = new MissileImpl(new Vector2(0, 0), 0, 0);
        final MissileImpl b = new MissileImpl(new Vector2(1000, 1000), 0, 0);

        engine.register(a);
        engine.register(b);
        engine.tick();

        assertTrue(listener.events.isEmpty(), "Distant missiles should not trigger a collision event");
    }

    @Test
    void unregisteredMissileDoesNotTriggerCollisionEvent() {
        final MissileImpl a = new MissileImpl(new Vector2(0, 0), 0, 0);
        final MissileImpl b = new MissileImpl(new Vector2(5, 0), 0, 0);

        engine.register(a);
        engine.register(b);
        engine.unregister(b);
        engine.tick();

        assertTrue(listener.events.isEmpty(), "Unregistered entities should not participate in collisions");
    }

    @Test
    void clearAllRemovesAllEntities() {
        final MissileImpl a = new MissileImpl(new Vector2(0, 0), 0, 0);
        final MissileImpl b = new MissileImpl(new Vector2(5, 0), 0, 0);

        engine.register(a);
        engine.register(b);
        engine.clearAll();
        engine.tick();

        assertTrue(listener.events.isEmpty(), "clearAll should remove all registered entities");
    }

    @Test
    void sameMissileDoesNotCollideWithItself() {
        final MissileImpl a = new MissileImpl(new Vector2(0, 0), 0, 0);

        engine.register(a);
        engine.tick();

        assertTrue(listener.events.isEmpty(), "A single entity must not collide with itself");
    }

    private Object payloadFor(final InternalEvent event) {
        final int index = listener.events.indexOf(event);
        assertTrue(index >= 0);
        return listener.payloads.get(index);
    }
}
