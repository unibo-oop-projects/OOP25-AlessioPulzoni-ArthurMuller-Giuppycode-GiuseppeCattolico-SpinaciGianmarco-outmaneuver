package outmaneuver.model.area.entity.missile;

import java.awt.Dimension;
import java.util.List;

import outmaneuver.model.area.collision.CollisionLayer;
import outmaneuver.model.area.collision.Hitbox;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.util.Vector2;

/**
 * Base implementation of {@link Missile} that provides shared steering, lifetime, slow
 * effect and out-of-bounds handling; concrete missile types customise behaviour by
 * overriding the relevant hooks.
 */
// CHECKSTYLE: AbstractClassName OFF
public abstract class MissileImpl implements Missile {
    // CHECKSTYLE: AbstractClassName ON

    // --- POSIZIONE E MOVIMENTO ---
    private Vector2 position;
    private Vector2 velocity;

    // --- PARAMETRI BASE (dal JSON) ---
    private final String type;
    private final double speed;
    private final double maxTurnAngle;
    private final double hitboxRadius;
    private final double predictionTime;
    private final int outOfBoundsMargin;

    // --- STATO ---
    private boolean alive;
    private double lifetime;

    // --- SLOW ---
    private boolean slowed;
    private double slowTimer;
    private double slowFactor = 1.0;

    /**
     * Creates a missile from its JSON-defined parameters.
     *
     * @param spawnPos the initial position in world coordinates
     * @param data the missile's type definition (speed, turn rate, lifetime, etc.)
     */
    protected MissileImpl(final Vector2 spawnPos, final MissileData data) {
        this.position = spawnPos;
        this.velocity = Vector2.ZERO;
        this.type = data.type();
        this.speed = data.speed();
        this.maxTurnAngle = data.maxTurn();
        this.hitboxRadius = data.radius();
        this.lifetime = data.lifetime();
        this.predictionTime = data.predictionTime();
        this.outOfBoundsMargin = data.outOfBoundsMargin();
        this.alive = true;
    }

    @Override
    public final void update(final Plane plane, final double dt) {
        if (shouldSkipUpdate(dt)) {
            return;
        }
        steer(plane.getPosition());
        move(dt);
    }

    /**
     * Updates lifetime and slow-effect timers and determines whether the rest of the
     * update (steering and movement) should be skipped this frame.
     *
     * @param dt elapsed time in seconds since the last update
     * @return {@code true} if the missile is dead or otherwise should not move this frame
     */
    protected final boolean shouldSkipUpdate(final double dt) {
        if (!alive) {
            return true;
        }

        if (lifetime >= 0) {
            lifetime -= dt;
            if (lifetime <= 0) {
                destroy();
                return true;
            }
        }

        if (slowed) {
            slowTimer -= dt;
            if (slowTimer <= 0) {
                slowed = false;
                slowFactor = 1.0;
            }
        }

        return false;
    }

    /**
     * Advances the missile's position according to its current velocity, applying the
     * slow-down factor if active.
     *
     * @param dt elapsed time in seconds since the last update
     */
    protected final void move(final double dt) {
        final double factor = slowed ? slowFactor : 1.0;
        position = position.add(velocity.scale(dt * factor));
    }

    /**
     * Steers the missile's velocity towards the given target, limited by its maximum
     * turn angle. Subclasses may override this to change or disable steering behaviour.
     *
     * @param target the point to steer towards, in world coordinates
     */
    // CHECKSTYLE: DesignForExtension OFF
    protected void steer(final Vector2 target) {
        final double desiredAngle = target.subtract(position).angle();
        final double currentAngle = velocity.angle();
        final double diff = normalizeAngle(desiredAngle - currentAngle);
        final double turn = Math.max(-maxTurnAngle, Math.min(maxTurnAngle, diff));
        velocity = Vector2.fromAngle(currentAngle + turn).scale(speed);
    }
    // CHECKSTYLE: DesignForExtension ON

    /**
     * Normalises an angle to the range {@code (-PI, PI]}.
     *
     * @param a the angle to normalise, in radians
     * @return the equivalent angle in the range {@code (-PI, PI]}
     */
    protected final double normalizeAngle(final double a) {
        final double twoPi = 2 * Math.PI;
        double normalised = a % twoPi;
        if (normalised > Math.PI) {
            normalised -= twoPi;
        } else if (normalised < -Math.PI) {
            normalised += twoPi;
        }
        return normalised;
    }

    @Override
    public final void setInitialDirection(final Vector2 target) {
        velocity = Vector2.fromAngle(target.subtract(position).angle()).scale(speed);
    }

    /**
     * Sets the missile's current velocity.
     *
     * @param vel the new velocity vector
     */
    protected final void setVelocity(final Vector2 vel) {
        this.velocity = vel;
    }

    /**
     * Returns the missile's current velocity.
     *
     * @return the current velocity vector
     */
    protected final Vector2 getVelocity() {
        return velocity;
    }

    @Override
    public final Vector2 getPosition() {
        return position;
    }

    @Override
    public final void setPosition(final Vector2 pos) {
        this.position = pos;
    }

    @Override
    public final void redirectIfOutOfBounds(final Plane plane, final Dimension screenSize, final double effectiveSpeed) {
        if (!isOffScreen(plane, screenSize)) {
            return;
        }
        final Vector2 planeVel = Vector2.fromAngle(plane.getDirection())
                .scale(effectiveSpeed);
        final Vector2 predicted = plane.getPosition().add(planeVel.scale(predictionTime));
        setInitialDirection(predicted);
    }

    /**
     * Returns the extra margin beyond the screen edge before the missile is considered
     * out of bounds.
     *
     * @return the out-of-bounds margin, in pixels
     */
    protected final int getOutOfBoundsMargin() {
        return outOfBoundsMargin;
    }

    // CHECKSTYLE: DesignForExtension OFF
    @Override
    public void onCollision(final List<Missile> activeMissiles) {
        destroy();
    }
    // CHECKSTYLE: DesignForExtension ON

    @Override
    public void checkBounce(final Vector2 planePos, final Dimension screenSize) { }

    /** Marks the missile as no longer alive. */
    protected final void destroy() {
        this.alive = false;
    }

    @Override
    public final boolean isAlive() {
        return alive;
    }

    @Override
    public final void slowDown(final double factor, final double duration) {
        this.slowed = true;
        this.slowFactor = factor;
        this.slowTimer = duration;
    }

    /**
     * Destroys the missile if it has moved beyond the playable area around the plane.
     *
     * @param plane the plane used as the reference point for the play area
     * @param screenSize the size of the visible play area
     */
    protected final void destroyIfOffScreen(final Plane plane, final Dimension screenSize) {
        if (isOffScreen(plane, screenSize)) {
            destroy();
        }
    }

    private boolean isOffScreen(final Plane plane, final Dimension screenSize) {
        final Vector2 delta = position.subtract(plane.getPosition());
        return Math.abs(delta.getX()) > screenSize.width / 2.0 + outOfBoundsMargin
            || Math.abs(delta.getY()) > screenSize.height / 2.0 + outOfBoundsMargin;
    }

    // --- ICollidable ---
    @Override
    public final Hitbox getHitbox() {
        return new Hitbox(position, hitboxRadius);
    }

    @Override
    public final CollisionLayer getCollisionLayer() {
        return CollisionLayer.MISSILE;
    }

    @Override
    public final String getMissileType() {
        return type;
    }

    @Override
    public final double getDirection() {
        return this.velocity.angle();
    }
}
