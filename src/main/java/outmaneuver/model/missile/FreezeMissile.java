package outmaneuver.model.missile;

import java.util.List;

/*
 * Quando collide con un altro missile congela tutti i missili nel raggio.
 * Spinaci chiama triggerFreeze() quando rileva la collisione.
 */
public final class FreezeMissile extends Missile {

    private static final double SPEED    = 350.0;
    private static final double MAX_TURN = 0.015;
    private static final double RADIUS   = 13.0;
    private static final double LIFETIME = 16.0;

    public static final double FREEZE_RADIUS   = 130.0;
    public static final double FREEZE_DURATION = 2.5;

    public FreezeMissile(final double x, final double y) {
        super(x, y, SPEED, MAX_TURN, RADIUS, LIFETIME);
    }

    // Chiamato da Spinaci quando rileva la collisione con un altro missile
    public void triggerFreeze(final List<Missile> others) {
        for (final Missile other : others) {
            if (!other.isAlive() || other == this) continue;
            final double dx = other.getWorldX() - getWorldX();
            final double dy = other.getWorldY() - getWorldY();
            if (dx * dx + dy * dy < FREEZE_RADIUS * FREEZE_RADIUS) {
                other.freeze(FREEZE_DURATION);
            }
        }
        destroy();
    }

    @Override
    protected double getMaxLifetime() { return LIFETIME; }

    @Override
    public String getMissileType() { return "freeze"; }
}