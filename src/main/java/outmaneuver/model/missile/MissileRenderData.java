package outmaneuver.model.missile;

/*
 * DTO — dati che il renderer legge per disegnare un missile.
 * Analogo a EntityRenderData per il piano.
 */
public final class MissileRenderData {

    private final double worldX;
    private final double worldY;
    private final double vx;
    private final double vy;
    private final double hitboxRadius;
    private final double lifetimeRatio; // 0.0-1.0, oppure -1 se infinito
    private final String missileType;

    public MissileRenderData(final double worldX, final double worldY,
                             final double vx, final double vy,
                             final double hitboxRadius, final double lifetimeRatio,
                             final String missileType) {
        this.worldX        = worldX;
        this.worldY        = worldY;
        this.vx            = vx;
        this.vy            = vy;
        this.hitboxRadius  = hitboxRadius;
        this.lifetimeRatio = lifetimeRatio;
        this.missileType   = missileType;
    }

    public double getWorldX()        { return worldX; }
    public double getWorldY()        { return worldY; }
    public double getVx()            { return vx; }
    public double getVy()            { return vy; }
    public double getHitboxRadius()  { return hitboxRadius; }
    public double getLifetimeRatio() { return lifetimeRatio; }
    public String getMissileType()   { return missileType; }
}