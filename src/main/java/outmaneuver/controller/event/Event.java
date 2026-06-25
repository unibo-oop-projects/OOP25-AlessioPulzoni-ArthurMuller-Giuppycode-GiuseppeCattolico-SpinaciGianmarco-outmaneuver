package outmaneuver.controller.event;

/**
 * Marker interface for internal game events dispatched through {@link InternalEventListener}.
 * Concrete events are typically enums (e.g. {@link GameEvent}, {@link CollisionEvent},
 * {@link EffectEvent}) implementing this interface.
 */
public interface Event {
}
