package outmaneuver.model.missile;

import outmaneuver.model.area.Plane;
import outmaneuver.util.Vector2;
import java.util.ArrayList;
import java.util.List;

/*
 * CLASSE ASTRATTA BASE PER TUTTI I MISSILI
 * Ogni missile ha:
 * - posizione nel mondo (worldX, worldY)
 * - movimento con velocità costante
 * - durata limitata (lifetime) o infinita (-1)
 * - hitbox circolare
 * - steering verso il piano nemico
 * - effetti speciali: freeze, slow
 */
public abstract class Missile {

    // --- POSIZIONE E MOVIMENTO ---
    protected double worldX;
    protected double worldY;
    protected double vx;
    protected double vy;

    // --- PARAMETRI BASE ---
    protected final double speed;
    protected final double maxTurnAngle;
    protected final double hitboxRadius;

    // --- STATO ---
    private boolean alive;
    private double lifetime;

    // --- FREEZE ---
    private boolean frozen     = false;
    private double  freezeTimer = 0;

    // --- SLOW (usato da ClockMissile) ---
    private boolean slowed     = false;
    private double  slowTimer  = 0;
    private double  slowFactor = 1.0;

    // COSTRUTTORE
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

    // --- UPDATE ---
    // Chiamato ogni frame dal MissileController con deltaSeconds
    public void update(final Plane plane, final double dt) {
        if (shouldSkipUpdate(dt)) return;
        steer(plane.getPosition().getX(), plane.getPosition().getY());
        move(dt);
    }

    // Decide se saltare l'update: lifetime scaduta, morto, congelato, rallentato
    protected final boolean shouldSkipUpdate(final double dt) {
        if (lifetime >= 0) {
            lifetime -= dt;
            if (lifetime <= 0) {
                destroy();
                return true;
            }
        }
        if (!alive) return true;

        // Freeze — missile completamente fermo
        if (frozen) {
            freezeTimer -= dt;
            if (freezeTimer <= 0) frozen = false;
            return true;
        }

        // Slow — aggiorna timer ma non blocca
        if (slowed) {
            slowTimer -= dt;
            if (slowTimer <= 0) {
                slowed     = false;
                slowFactor = 1.0;
            }
        }

        return false;
    }

    // --- MOVIMENTO ---
    protected final void move(final double dt) {
        final double factor = slowed ? slowFactor : 1.0;
        worldX += vx * dt * factor;
        worldY += vy * dt * factor;
    }

    // --- STEERING ---
    // Ruota il missile verso il bersaglio rispettando maxTurnAngle
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

    // --- DIREZIONE INIZIALE ---
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

    // --- SPAWN ON DEATH ---
    // Override in SplitMissile e TwinsMissile
    public List<Missile> getSpawnOnDeath() {
        return new ArrayList<>();
    }

    // --- STATO ---
    public void destroy()        { this.alive = false; }
    public boolean isAlive()     { return alive; }

    public void freeze(final double duration) {
        this.frozen      = true;
        this.freezeTimer = duration;
    }

    public boolean isFrozen()    { return frozen; }

    public void slowDown(final double factor, final double duration) {
        this.slowed     = true;
        this.slowFactor = factor;
        this.slowTimer  = duration;
    }

    public boolean isSlowed()    { return slowed; }

    // --- HITBOX ---
    // Controlla collisione circolare con il piano
    public boolean collidesWith(final Plane plane) {
        final double dx = plane.getPosition().getX() - worldX;
        final double dy = plane.getPosition().getY() - worldY;
        final double dist = Math.sqrt(dx * dx + dy * dy);
        return dist < hitboxRadius + plane.getStats().getHitboxRadius();
    }

    // --- FUORI SCHERMO ---
    // Un missile è fuori schermo se è troppo lontano dal piano (centro camera)
    public boolean isOffScreen(final Plane plane, final int screenW, final int screenH) {
        final double dx = worldX - plane.getPosition().getX();
        final double dy = worldY - plane.getPosition().getY();
        final int margin = 150;
        return Math.abs(dx) > screenW / 2.0 + margin
            || Math.abs(dy) > screenH / 2.0 + margin;
    }

    // --- GETTERS ---
    public double getWorldX()       { return worldX; }
    public double getWorldY()       { return worldY; }
    public double getVx()           { return vx; }
    public double getVy()           { return vy; }
    public double getSpeed()        { return speed; }
    public double getHitboxRadius() { return hitboxRadius; }

    // Override nelle sottoclassi per la barra lifetime
    protected double getMaxLifetime() { return -1; }

    // Restituisce un DTO con i dati necessari al renderer
    public MissileRenderData getRenderData() {
        return new MissileRenderData(worldX, worldY, vx, vy,
                hitboxRadius, getMaxLifetime() > 0 ? lifetime / getMaxLifetime() : -1,
                getMissileType());
    }

    // Override nelle sottoclassi per identificare il tipo al renderer
    public abstract String getMissileType();
}