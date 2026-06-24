package outmaneuver.model.area.entity.plane;

import java.util.Objects;
import outmaneuver.model.area.collision.CollisionLayer;
import outmaneuver.model.area.collision.Hitbox;
import outmaneuver.util.Vector2;

public final class PlaneImpl implements Plane {

    private Vector2 position;
    private double direction;
    private PlaneStats stats;
    private TurnState turnState;

    public PlaneImpl(final PlaneStats stats) {
        this.position = Vector2.ZERO;
        this.direction = 0;
        this.stats = Objects.requireNonNull(stats, "stats must not be null");
        this.turnState = TurnState.NONE;
    }

    @Override
    public Vector2 getPosition() {
        return position;
    }

    @Override
    public void setPosition(final Vector2 position) {
        this.position = Objects.requireNonNull(position, "position must not be null");
    }

    @Override
    public double getDirection() {
        return direction;
    }

    @Override
    public void setDirection(final double direction) {
        this.direction = direction;
    }

    @Override
    public PlaneStats getStats() {
        return stats;
    }

    @Override
    public void setStats(final PlaneStats stats) {
        this.stats = Objects.requireNonNull(stats, "stats must not be null");
    }

    @Override
    public TurnState getTurnState() {
        return turnState;
    }

    @Override
    public void setTurnState(final TurnState state) {
        this.turnState = Objects.requireNonNull(state, "turnState must not be null");
    }

    @Override
    public Hitbox getHitbox() {
        return new Hitbox(position, stats.getHitboxRadius());
    }

    @Override
    public CollisionLayer getCollisionLayer() {
        return CollisionLayer.PLANE;
    }
}
