package outmaneuver.controller.event;

/**
 * Receives internal game events (collisions, effects, state changes) dispatched by
 * controllers. Implementers decide how to react based on the concrete {@link Event}
 * type and the accompanying payload.
 */
@FunctionalInterface
public interface InternalEventListener {

    /**
     * Handles a dispatched internal event.
     *
     * @param evt the event that occurred
     * @param data the payload associated with the event, or {@code null} if none;
     *     its type depends on the concrete {@code evt}
     */
    void onInternalEvent(Event evt, Object data);
}
