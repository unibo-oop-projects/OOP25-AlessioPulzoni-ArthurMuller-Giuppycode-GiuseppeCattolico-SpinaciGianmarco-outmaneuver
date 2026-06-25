package outmaneuver.controller.event;

/** Identifies which pair of collision layers collided, reported via {@link InternalEventListener}. */
public enum CollisionEvent implements Event {
    MISSILE_MISSILE_COLLISION,
    PLANE_MISSILE_COLLISION,
    PLANE_COLLECTIBLE_COLLISION
}
