package outmaneuver.model.missile;

import java.util.ArrayList;
import java.util.List;

import outmaneuver.model.area.Plane;
import outmaneuver.view.MissileRenderData;

public abstract class Missile implements IMissile {

    // --- POSIZIONE E MOVIMENTO ---
    protected double worldX;
    protected double worldY;
    protected double vx;
    protected double vy;

    // --- PARAMETRI BASE ---
    protected final double speed;
    protected final double maxTurnAngle;
    protected final double hitboxRadius;

    // --- REDIRECT ---
    private static final double PREDICTION_TIME = 0.8;

    // --- STATO ---
    private boolean alive;
    private double lifetime;

    // --- FREEZE ---
    private boolean frozen      = false;
    private double  freezeTimer = 0;

    // --- SLOW ---
    private boolean slowed     = false;
    private double  slowTimer  = 0;
    private double  slowFactor = 1.0;

    protected Missile(final double worldX, final double worldY,
                      final double speed, final double maxTurnAngle,
                      final double hitboxRadius, final double lifetime) {
        this.worldX       = worldX;
        this.worldY       = worldY;
        this.speed        = speed;
        this.maxTurnAngle = maxTurnAngle;
        this.hitboxRadius = hitboxRadius;
        this.lifetime     = lifetime;
        this.alive        = true;
        this.vx           = 0;
        this.vy           = 0;
    }

    @Override
    public void update(final Plane plane, final double dt) {
        if (shouldSkipUpdate(dt)) return;
        steer(plane.getPosition().getX(), plane.getPosition().getY());
        move(dt);
    }

    protected final boolean shouldSkipUpdate(final double dt) {
        if (lifetime >= 0) {
            lifetime -= dt;
            if (lifetime <= 0) {
                destroy();
                return true;
            }
        }
        if (!alive) return true;

        if (frozen) {
            freezeTimer -= dt;
            if (freezeTimer <= 0) frozen = false;
            return true;
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
        worldX += vx * dt * factor;
        worldY += vy * dt * factor;
    }

    protected void steer(final double tx, final double ty) {
        final double dx           = tx - worldX;
        final double dy           = ty - worldY;
        final double desiredAngle = Math.atan2(dy, dx);
        final double currentAngle = Math.atan2(vy, vx);
        final double diff         = normalizeAngle(desiredAngle - currentAngle);
        final double turn         = Math.max(-maxTurnAngle, Math.min(maxTurnAngle, diff));
        final double newAngle     = currentAngle + turn;
        vx = Math.cos(newAngle) * speed;
        vy = Math.sin(newAngle) * speed;
    }

    protected final double normalizeAngle(double a) {
        while (a >  Math.PI) a -= 2 * Math.PI;
        while (a < -Math.PI) a += 2 * Math.PI;
        return a;
    }

    @Override
    public void setInitialDirection(final double targetX, final double targetY) {
        final double angle = Math.atan2(targetY - worldY, targetX - worldX);
        vx = Math.cos(angle) * speed;
        vy = Math.sin(angle) * speed;
    }

    public void setVelocity(final double vx, final double vy) {
        this.vx = vx;
        this.vy = vy;
    }

    public void setWorldPosition(final double x, final double y) {
        this.worldX = x;
        this.worldY = y;
    }

    @Override
    public boolean redirectIfOutOfBounds(final Plane plane,
                                          final int screenW, final int screenH) {
        final double dx = worldX - plane.getPosition().getX();
        final double dy = worldY - plane.getPosition().getY();
        final int margin = 150;
        final boolean outX = Math.abs(dx) > screenW / 2.0 + margin;
        final boolean outY = Math.abs(dy) > screenH / 2.0 + margin;

        if (outX || outY) {
            final double planeVx = plane.getEffectiveSpeed() * Math.cos(plane.getDirection());
            final double planeVy = plane.getEffectiveSpeed() * Math.sin(plane.getDirection());
            final double predictedX = plane.getPosition().getX() + planeVx * PREDICTION_TIME;
            final double predictedY = plane.getPosition().getY() + planeVy * PREDICTION_TIME;
            setInitialDirection(predictedX, predictedY);
            return true;
        }
        return false;
    }

    @Override
    public List<IMissile> getSpawnOnDeath() { return new ArrayList<>(); }

    @Override
    public void destroy()        { this.alive = false; }

    @Override
    public boolean isAlive()     { return alive; }

    @Override
    public void freeze(final double duration) {
        this.frozen      = true;
        this.freezeTimer = duration;
    }

    public boolean isFrozen() { return frozen; }

    @Override
    public void slowDown(final double factor, final double duration) {
        this.slowed     = true;
        this.slowFactor = factor;
        this.slowTimer  = duration;
    }

    public boolean isSlowed() { return slowed; }

    @Override
    public boolean collidesWith(final Plane plane) {
        final double dx   = plane.getPosition().getX() - worldX;
        final double dy   = plane.getPosition().getY() - worldY;
        final double dist = Math.sqrt(dx * dx + dy * dy);
        return dist < hitboxRadius + plane.getStats().getHitboxRadius();
    }

    public boolean isOffScreen(final Plane plane, final int screenW, final int screenH) {
        final double dx = worldX - plane.getPosition().getX();
        final double dy = worldY - plane.getPosition().getY();
        final int margin = 150;
        return Math.abs(dx) > screenW / 2.0 + margin
            || Math.abs(dy) > screenH / 2.0 + margin;
    }

    @Override
    public double getWorldX()       { return worldX; }
    @Override
    public double getWorldY()       { return worldY; }
    public double getVx()           { return vx; }
    public double getVy()           { return vy; }
    public double getSpeed()        { return speed; }
    @Override
    public double getHitboxRadius() { return hitboxRadius; }

    protected double getMaxLifetime() { return -1; }

    @Override
    public boolean isGhostVisible() { return true; }

    @Override
    public MissileRenderData getRenderData() {
        return new MissileRenderData(
                worldX, worldY, vx, vy,
                hitboxRadius,
                getMaxLifetime() > 0 ? lifetime / getMaxLifetime() : -1,
                getMissileType(),
                isGhostVisible());
    }

    @Override
    public abstract String getMissileType();
}