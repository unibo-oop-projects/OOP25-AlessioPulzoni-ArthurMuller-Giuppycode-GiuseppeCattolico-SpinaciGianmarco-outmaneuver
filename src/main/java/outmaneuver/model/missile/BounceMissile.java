package outmaneuver.model.missile;

import java.util.Random;

import outmaneuver.model.area.Plane;

/*
 * Rimbalza sui bordi dello schermo con direzione casuale.
 * Non insegue l'aereo — va per conto suo.
 * Muore solo per collisione, non ha lifetime.
 */
public final class BounceMissile extends Missile {

    private static final double SPEED         = 400.0;
    private static final double RADIUS        = 11.0;
    private static final int    BOUNCE_MARGIN = 10;

    private final int screenW;
    private final int screenH;

    public BounceMissile(final double x, final double y,
                         final int screenW, final int screenH) {
        super(x, y, SPEED, 0.0, RADIUS, -1);
        this.screenW = screenW;
        this.screenH = screenH;

        // Direzione iniziale casuale
        final double angle = new Random().nextDouble() * Math.PI * 2;
        setVelocity(Math.cos(angle) * SPEED, Math.sin(angle) * SPEED);
    }

    // Non sterza — solo movimento e rimbalzo
    @Override
    public void update(final Plane plane, final double dt) {
        if (shouldSkipUpdate(dt)) return;
        move(dt);
    }

    // Chiamato dal MissileControllerImpl ogni frame
    public void checkBounce(final Plane plane) {
        final double cx = plane.getPosition().getX();
        final double cy = plane.getPosition().getY();

        final double sx = getWorldX() - cx;
        final double sy = getWorldY() - cy;

        if (sx < -screenW / 2.0 + BOUNCE_MARGIN) {
            setVelocity(Math.abs(getVx()), getVy());
        } else if (sx > screenW / 2.0 - BOUNCE_MARGIN) {
            setVelocity(-Math.abs(getVx()), getVy());
        }

        if (sy < -screenH / 2.0 + BOUNCE_MARGIN) {
            setVelocity(getVx(), Math.abs(getVy()));
        } else if (sy > screenH / 2.0 - BOUNCE_MARGIN) {
            setVelocity(getVx(), -Math.abs(getVy()));
        }
    }

    // Non esce mai dallo schermo
    @Override
    public boolean isOffScreen(final Plane plane, final int sw, final int sh) {
        return false;
    }

    @Override
    public String getMissileType() { return "bounce"; }
}