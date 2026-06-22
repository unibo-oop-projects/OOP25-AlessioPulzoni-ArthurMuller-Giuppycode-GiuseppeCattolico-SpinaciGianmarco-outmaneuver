package outmaneuver.controller.event;

import outmaneuver.controller.CollisionEvent;

@FunctionalInterface
public interface InternalEventListener {
    void onInternalEvent(CollisionEvent evt, Object data);
}
