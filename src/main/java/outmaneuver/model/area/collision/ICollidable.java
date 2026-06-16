package outmaneuver.model.area.collision;

/** Minimal contract to participate in collisions. */
public interface ICollidable {
    /** Return the entity's hitbox in world coordinates. */
    Hitbox getHitbox();

    /** Return collision layer (used to filter collisions). */
    CollisionLayer getCollisionLayer();
}


