package outmaneuver.model.area.entity.collectibles;

import java.util.Objects;

import outmaneuver.model.area.collision.CollisionLayer;
import outmaneuver.model.area.collision.Hitbox;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.session.IGameSession;
import outmaneuver.util.Vector2;

public abstract class AbstractCollectible implements Collectible {

    private static final double HITBOX_RADIUS = 10.0;

    Vector2 position;


     public AbstractCollectible(Vector2 position) {
        this.position = Objects.requireNonNull(position, "position must not be null");
    }

    @Override
    public Vector2 getPosition() {
        return position;
    }
    

    @Override
    public void setPosition(Vector2 position) {
        this.position = Objects.requireNonNull(position, "position must not be null");
    }

    @Override
    public CollisionLayer getCollisionLayer() {
        return CollisionLayer.COLLECTIBLE;
    }

    @Override
    public Hitbox getHitbox() {
        return new Hitbox(position, HITBOX_RADIUS);
    }

    @Override
    public void apply(Plane plane, IGameSession session) {
        // Default implementation does nothing
    }

}
