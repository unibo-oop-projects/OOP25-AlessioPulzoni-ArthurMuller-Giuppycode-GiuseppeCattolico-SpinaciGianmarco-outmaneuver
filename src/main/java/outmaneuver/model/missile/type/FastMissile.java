package outmaneuver.model.missile.type;

import outmaneuver.model.missile.Missile;

/*
 * Veloce e agile — più difficile da evitare del Basic.
 */
public final class FastMissile extends Missile {

    private static final double SPEED    = 450.0;
    private static final double MAX_TURN = 0.01;
    private static final double RADIUS   = 8.0;
    private static final double LIFETIME = 8.0;

    public FastMissile(final double x, final double y) {
        super(x, y, SPEED, MAX_TURN, RADIUS, LIFETIME);
    }

    @Override
    protected double getMaxLifetime() { return LIFETIME; }

    @Override
    public String getMissileType() { return "fast"; }
}