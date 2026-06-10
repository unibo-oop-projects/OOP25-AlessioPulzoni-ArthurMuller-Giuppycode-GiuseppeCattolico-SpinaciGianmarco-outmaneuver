package outmaneuver.model.missile.type;

import java.util.List;

import outmaneuver.model.missile.IMissile;
import outmaneuver.model.missile.Missile;

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

    public void triggerSlow(final List<IMissile> others) {
        for (final IMissile other : others) {
            if (!other.isAlive() || other.equals(this)) continue;
            other.slowDown(SLOW_FACTOR, SLOW_DURATION);
        }
        destroy();
    }

    @Override
    protected double getMaxLifetime() { return LIFETIME; }

    @Override
    public String getMissileType() { return "clock"; }
}