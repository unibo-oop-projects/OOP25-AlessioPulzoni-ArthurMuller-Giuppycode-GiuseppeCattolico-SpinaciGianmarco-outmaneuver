package outmaneuver.model.collision;

import outmaneuver.util.Vector2;

public final class CollisionData {
    private final ICollidable entityA;
    private final ICollidable entityB;
    private final Vector2 collisionPoint;

    public CollisionData(ICollidable entityA, ICollidable entityB,
                         Vector2 collisionPoint) {
        this.entityA    = entityA;
        this.entityB    = entityB;
        this.collisionPoint = collisionPoint;
    }

    public ICollidable getEntityA()    { return entityA; }
    public ICollidable getEntityB()    { return entityB; }
    public Vector2     getCollisionPoint() { return collisionPoint; }
}
