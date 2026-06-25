package outmaneuver.model.area.entity.collectibles;

import java.util.Objects;

import outmaneuver.model.area.collision.CollisionLayer;
import outmaneuver.model.area.collision.Hitbox;
import outmaneuver.model.area.effect.Effect;
import outmaneuver.util.Vector2;

/** Base implementation shared by collectible pickups, providing position and hitbox handling. */
public abstract class AbstractCollectible implements Collectible {

    private static final double HITBOX_RADIUS = 15.0;
    private static final String POSITION_NOT_NULL = "position must not be null";

    private Vector2 position;
    private Effect effect;

    /**
     * Creates a collectible with no associated effect.
     *
     * @param position the spawn position in world coordinates
     */
    public AbstractCollectible(final Vector2 position) {
        this.position = Objects.requireNonNull(position, POSITION_NOT_NULL);
    }

    /**
     * Creates a collectible that grants the given effect when picked up.
     *
     * @param position the spawn position in world coordinates
     * @param effect the effect granted to the plane that collects this entity
     */
    public AbstractCollectible(final Vector2 position, final Effect effect) {
        this.position = Objects.requireNonNull(position, POSITION_NOT_NULL);
        this.effect = Objects.requireNonNull(effect, "effect must not be null");
    }

    @Override
    public final Vector2 getPosition() {
        return position;
    }

    @Override
    public final void setPosition(final Vector2 position) {
        this.position = Objects.requireNonNull(position, POSITION_NOT_NULL);
    }

    @Override
    public final CollisionLayer getCollisionLayer() {
        return CollisionLayer.COLLECTIBLE;
    }

    @Override
    public final Hitbox getHitbox() {
        return new Hitbox(position, HITBOX_RADIUS);
    }

    @Override
    public final Effect getEffect() {
        return this.effect;
    }
}
