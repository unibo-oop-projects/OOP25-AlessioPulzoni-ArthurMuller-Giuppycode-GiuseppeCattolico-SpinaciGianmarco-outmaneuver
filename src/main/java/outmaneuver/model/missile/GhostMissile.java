package outmaneuver.model.missile;

import outmaneuver.model.area.Plane;

/*
 * Alterna tra visibile e invisibile ogni tot secondi.
 * La hitbox è SEMPRE attiva — anche invisibile può colpire.
 * Il renderer usa isGhostVisible() per decidere l'opacità.
 */
public final class GhostMissile extends Missile {

    private static final double SPEED              = 350.0;
    private static final double MAX_TURN           = 0.015;
    private static final double RADIUS             = 10.0;
    private static final double LIFETIME           = 18.0;
    private static final double VISIBLE_DURATION   = 1.5;
    private static final double INVISIBLE_DURATION = 2.0;

    private double  phaseTimer   = 0;
    private boolean ghostVisible = true;

    public GhostMissile(final double x, final double y) {
        super(x, y, SPEED, MAX_TURN, RADIUS, LIFETIME);
    }

    @Override
    public void update(final Plane plane, final double dt) {
        if (shouldSkipUpdate(dt)) return;

        // Aggiorna fase visibile/invisibile
        phaseTimer += dt;
        final double phaseDuration = ghostVisible ? VISIBLE_DURATION : INVISIBLE_DURATION;
        if (phaseTimer >= phaseDuration) {
            ghostVisible = !ghostVisible;
            phaseTimer   = 0;
        }

        steer(plane.getPosition().getX(), plane.getPosition().getY());
        move(dt);
    }

    public boolean isGhostVisible() { return ghostVisible; }

    @Override
    protected double getMaxLifetime() { return LIFETIME; }

    @Override
    public String getMissileType() { return "ghost"; }
}