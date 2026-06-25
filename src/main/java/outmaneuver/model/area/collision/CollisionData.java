package outmaneuver.model.area.collision;

import outmaneuver.util.Vector2;

/** Holds the two entities involved in a collision and where it occurred. */
public final class CollisionData {
    private final ICollidable entityA;
    private final ICollidable entityB;
    private final Vector2 collisionPoint;

    /**
     * Creates a new collision data record.
     *
     * @param entityA the first entity involved in the collision
     * @param entityB the second entity involved in the collision
     * @param collisionPoint the point in world coordinates where the collision occurred
     */
    public CollisionData(final ICollidable entityA, final ICollidable entityB,
                         final Vector2 collisionPoint) {
        this.entityA = entityA;
        this.entityB = entityB;
        this.collisionPoint = collisionPoint;
    }

    /**
     * Returns the first entity involved in the collision.
     *
     * @return the first colliding entity
     */
    public ICollidable getEntityA() {
        return entityA;
    }

    /**
     * Returns the second entity involved in the collision.
     *
     * @return the second colliding entity
     */
    public ICollidable getEntityB() {
        return entityB;
    }

    /**
     * Returns the point where the collision occurred.
     *
     * @return the collision point in world coordinates
     */
    public Vector2 getCollisionPoint() {
        return collisionPoint;
    }
}
