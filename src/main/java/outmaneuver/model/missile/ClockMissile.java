package outmaneuver.model.missile;

import java.util.List;

/*
 * Quando collide con un altro missile rallenta tutti i missili attivi.
 * Spinaci chiama triggerSlow() quando rileva la collisione.
 */
public final class ClockMissile extends Missile {

    private static final double SPEED    = 350.0;
    private static final double MAX_TURN = 0.015;
    private static final double RADIUS   = 12.0;
    private static final double LIFETIME = 15.0;

    public static final double SLOW_DURATION = 3.0;
    public static final double SLOW_FACTOR   = 0.3;

    public ClockMissile(final double x, final double y) {
        super(x, y, SPEED, MAX_TURN, RADIUS, LIFETIME);
    }

    // Chiamato da Spinaci quando rileva la collisione con un altro missile
    public void triggerSlow(final List<Missile> others) {
        for (final Missile other : others) {
            if (!other.isAlive() || other == this) continue;
            other.slowDown(SLOW_FACTOR, SLOW_DURATION);
        }
        destroy();
    }

    @Override
    protected double getMaxLifetime() { return LIFETIME; }

    @Override
    public String getMissileType() { return "clock"; }
}