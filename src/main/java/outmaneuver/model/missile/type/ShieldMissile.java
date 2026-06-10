package outmaneuver.model.missile.type;

import outmaneuver.model.missile.Missile;

/*
 * Richiede due collisioni per essere distrutto.
 * Prima collisione — rompe lo scudo.
 * Seconda collisione — distrugge il missile.
 * Spinaci chiama hit() invece di destroy().
 */
public final class ShieldMissile extends Missile {

    private static final double SPEED    = 350.0;
    private static final double MAX_TURN = 0.015;
    private static final double RADIUS   = 11.0;
    private static final double LIFETIME = 14.0;

    public static final double SHIELD_RADIUS = 22.0;

    private boolean shielded = true;

    public ShieldMissile(final double x, final double y) {
        super(x, y, SPEED, MAX_TURN, RADIUS, LIFETIME);
    }

    public void hit() {
        if (shielded) {
            shielded = false;
        } else {
            destroy();
        }
    }

    public boolean isShielded() { return shielded; }

    @Override
    protected double getMaxLifetime() { return LIFETIME; }

    @Override
    public String getMissileType() { return "shield"; }
}