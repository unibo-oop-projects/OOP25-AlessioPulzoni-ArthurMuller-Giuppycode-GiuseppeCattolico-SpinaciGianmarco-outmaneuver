package outmaneuver.model.area.entity.collectibles;

import java.util.Objects;

import outmaneuver.model.area.collision.CollisionLayer;
import outmaneuver.model.area.collision.Hitbox;
import outmaneuver.model.area.effect.Effect;
import outmaneuver.util.Vector2;

public abstract class AbstractCollectible implements Collectible {

    private static final double HITBOX_RADIUS = 10.0;

    Vector2 position;
    Effect effect;

    public AbstractCollectible(Vector2 position) {
        this.position = Objects.requireNonNull(position, "position must not be null");
    }

    public AbstractCollectible(Vector2 position, Effect effect) {
        this.position = Objects.requireNonNull(position, "position must not be null");
        this.effect = Objects.requireNonNull(effect, "effect must not be null");
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
    public Effect getEffect() {
        return this.effect;
    }
}
