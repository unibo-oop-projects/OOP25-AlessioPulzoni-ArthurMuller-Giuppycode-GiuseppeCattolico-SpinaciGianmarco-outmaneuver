package outmaneuver.model.area.entity.missile;

import java.awt.Dimension;
import java.util.List;

import outmaneuver.model.area.collision.CollisionLayer;
import outmaneuver.model.area.collision.Hitbox;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.util.Vector2;

public abstract class MissileImpl implements Missile {

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
    private double  slowTimer;
    private double  slowFactor = 1.0;

    protected MissileImpl(final Vector2 spawnPos, final MissileData data) {
        this.position          = spawnPos;
        this.velocity          = Vector2.ZERO;
        this.type              = data.type();
        this.speed             = data.speed();
        this.maxTurnAngle      = data.maxTurn();
        this.hitboxRadius      = data.radius();
        this.lifetime          = data.lifetime();
        this.predictionTime    = data.predictionTime();
        this.outOfBoundsMargin = data.outOfBoundsMargin();
        this.alive             = true;
    }

    @Override
    public void update(final Plane plane, final double dt) {
        if (shouldSkipUpdate(dt)) return;
        steer(plane.getPosition());
        move(dt);
    }

    protected final boolean shouldSkipUpdate(final double dt) {
        if (!alive) return true;

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
                slowed     = false;
                slowFactor = 1.0;
            }
        }

        return false;
    }

    protected final void move(final double dt) {
        final double factor = slowed ? slowFactor : 1.0;
        position = position.add(velocity.scale(dt * factor));
    }

    protected void steer(final Vector2 target) {
        final double desiredAngle = target.subtract(position).angle();
        final double currentAngle = velocity.angle();
        final double diff         = normalizeAngle(desiredAngle - currentAngle);
        final double turn         = Math.max(-maxTurnAngle, Math.min(maxTurnAngle, diff));
        velocity = Vector2.fromAngle(currentAngle + turn).scale(speed);
    }

    protected final double normalizeAngle(double a) {
        while (a >  Math.PI) a -= 2 * Math.PI;
        while (a < -Math.PI) a += 2 * Math.PI;
        return a;
    }

    @Override
    public void setInitialDirection(final Vector2 target) {
        velocity = Vector2.fromAngle(target.subtract(position).angle()).scale(speed);
    }

    protected void setVelocity(final Vector2 vel) {
        this.velocity = vel;
    }

    protected Vector2 getVelocity() {
        return velocity;
    }

    @Override
    public Vector2 getPosition() {
        return position;
    }

    @Override
    public void setPosition(final Vector2 pos) {
        this.position = pos;
    }

    @Override
    public void redirectIfOutOfBounds(final Plane plane, final Dimension screenSize, double effectiveSpeed) {
        if (!isOffScreen(plane, screenSize)) {
            return;
        }
        final Vector2 planeVel = Vector2.fromAngle(plane.getDirection())
                .scale(effectiveSpeed);
        final Vector2 predicted = plane.getPosition().add(planeVel.scale(predictionTime));
        setInitialDirection(predicted);
    }

    protected int getOutOfBoundsMargin() { return outOfBoundsMargin; }

    @Override
    public void onCollision(final List<Missile> activeMissiles) { destroy(); }

    @Override
    public void checkBounce(final Vector2 planePos, final Dimension screenSize) { }

    protected void destroy() { this.alive = false; }

    @Override
    public boolean isAlive() { return alive; }

    @Override
    public void slowDown(final double factor, final double duration) {
        this.slowed     = true;
        this.slowFactor = factor;
        this.slowTimer  = duration;
    }

    protected final void destroyIfOffScreen(final Plane plane, final Dimension screenSize) {
        if (isOffScreen(plane, screenSize)) {
            destroy();
        }
    }

    private boolean isOffScreen(final Plane plane, final Dimension screenSize) {
        final Vector2 delta = position.subtract(plane.getPosition());
        return Math.abs(delta.getX()) > screenSize.width  / 2.0 + outOfBoundsMargin
            || Math.abs(delta.getY()) > screenSize.height / 2.0 + outOfBoundsMargin;
    }

    // --- ICollidable ---
    @Override
    public Hitbox getHitbox() {
        return new Hitbox(position, hitboxRadius);
    }

    @Override
    public CollisionLayer getCollisionLayer() {
        return CollisionLayer.MISSILE;
    }

    @Override
    public String getMissileType() {
        return type;
    }

    @Override
    public double getDirection() {
        return this.velocity.angle();
    }
}
