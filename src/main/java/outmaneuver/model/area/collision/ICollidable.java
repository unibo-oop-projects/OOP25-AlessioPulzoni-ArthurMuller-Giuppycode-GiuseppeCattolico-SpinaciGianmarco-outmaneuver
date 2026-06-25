package outmaneuver.model.area.collision;

/** Minimal contract to participate in collisions. */
public interface ICollidable {
    /**
     * Return the entity's hitbox in world coordinates.
     *
     * @return the entity's hitbox
     */
    Hitbox getHitbox();

    /**
     * Return collision layer (used to filter collisions).
     *
     * @return the collision layer this entity belongs to
     */
    CollisionLayer getCollisionLayer();
}


