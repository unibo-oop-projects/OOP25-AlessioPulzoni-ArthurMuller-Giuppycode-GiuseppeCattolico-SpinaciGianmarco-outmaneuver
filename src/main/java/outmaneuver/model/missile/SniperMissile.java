package outmaneuver.model.missile;

import outmaneuver.model.area.Plane;

/*
 * Direzione fissa al momento dello spawn — non insegue.
 * Velocissimo, va dritto dove puntava l'aereo al momento dello spawn.
 * Se esce dallo schermo viene eliminato — non viene rediretto.
 */
public final class SniperMissile extends Missile {

    private static final double SPEED  = 580.0;
    private static final double RADIUS = 6.0;

    public SniperMissile(final double x, final double y, final Plane plane) {
        super(x, y, SPEED, 0.0, RADIUS, -1);
        final double angle = Math.atan2(
                plane.getPosition().getY() - y,
                plane.getPosition().getX() - x);
        setVelocity(Math.cos(angle) * SPEED, Math.sin(angle) * SPEED);
    }

    // Non sterza — va sempre dritto
    @Override
    public void update(final Plane plane, final double dt) {
        if (shouldSkipUpdate(dt)) return;
        move(dt);
    }

    // Se esce dallo schermo viene eliminato, non rediretto
    @Override
    public boolean redirectIfOutOfBounds(final Plane plane,
                                          final int screenW, final int screenH) {
        if (isOffScreen(plane, screenW, screenH)) {
            destroy();
        }
        return false;
    }

    @Override
    public String getMissileType() { return "sniper"; }
}