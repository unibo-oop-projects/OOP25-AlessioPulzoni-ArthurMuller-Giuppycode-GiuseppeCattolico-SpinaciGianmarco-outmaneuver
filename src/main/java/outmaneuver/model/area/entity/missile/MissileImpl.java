package outmaneuver.model.area.entity.missile;

import outmaneuver.model.collision.CollisionLayer;
import outmaneuver.model.collision.Hitbox;
import outmaneuver.util.Vector2;
import java.util.Objects;

/**
 * Represents a missile entity in the game world.
 * Missiles move in a straight line and can collide with entities.
 */
public final class MissileImpl implements Missile {

    private static final double HITBOX_RADIUS = 8.0;

    private Vector2 position;
    private double direction;
    private double speed;

    /**
     * Creates a new missile at the specified position and direction.
     *
     * @param position the initial position
     * @param direction the direction in radians
     * @param speed the movement speed
     */
    public MissileImpl(final Vector2 position, final double direction, final double speed) {
        this.position = Objects.requireNonNull(position, "position must not be null");
        this.direction = direction;
        this.speed = speed;
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
    public double getSpeed() {
        return speed;
    }

    @Override
    public void setSpeed(final double speed) {
        this.speed = speed;
    }

    @Override
    public void update(final long deltaMs) {
        final double deltaSec = deltaMs / 1000.0;
        final Vector2 velocity = Vector2.fromAngle(direction).scale(speed);
        setPosition(position.add(velocity.scale(deltaSec)));
    }


    @Override
    public Hitbox getHitbox() {
        return new Hitbox(position, HITBOX_RADIUS);
    }

    @Override
    public CollisionLayer getCollisionLayer() {
        return CollisionLayer.MISSILE;
    }
}
