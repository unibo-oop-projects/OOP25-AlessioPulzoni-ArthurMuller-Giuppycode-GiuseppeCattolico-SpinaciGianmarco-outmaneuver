package outmaneuver.controller.event;

import outmaneuver.controller.InternalEvent;

@FunctionalInterface
public interface InternalEventListener {
    void onInternalEvent(InternalEvent evt, Object data);
}
