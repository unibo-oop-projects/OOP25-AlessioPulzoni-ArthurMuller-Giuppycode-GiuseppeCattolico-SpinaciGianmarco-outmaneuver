package outmaneuver.model.area;

import outmaneuver.util.Vector2;
import java.util.Objects;

public final class PlaneImpl implements Plane {

    private Vector2 position;
    private double direction;
    private PlaneStats stats;
    private TurnState turnState;
    private boolean shieldActive;
    private double speedMultiplier;
    private long multiplierEndTime;

    public PlaneImpl(final PlaneStats stats) {
        this.position = Vector2.ZERO;
        this.direction = 0;
        this.stats = Objects.requireNonNull(stats, "stats must not be null");
        this.turnState = TurnState.NONE;
        this.shieldActive = false;
        this.speedMultiplier = 1.0;
        this.multiplierEndTime = 0;
    }

    @Override
    public Vector2 getPosition() {
        return position;
    }

    @Override
    public void setPosition(final Vector2 position) {
        this.position = Objects.requireNonNull(position, "position must not be null");
    }

    @Override
    public double getDirection() {
        return direction;
    }

    @Override
    public void setDirection(final double direction) {
        this.direction = direction;
    }

    @Override
    public PlaneStats getStats() {
        return stats;
    }

    @Override
    public void setStats(final PlaneStats stats) {
        this.stats = Objects.requireNonNull(stats, "stats must not be null");
    }

    @Override
    public TurnState getTurnState() {
        return turnState;
    }

    @Override
    public void setTurnState(final TurnState state) {
        this.turnState = Objects.requireNonNull(state, "turnState must not be null");
    }

    @Override
    public boolean isShieldActive() {
        return shieldActive;
    }

    @Override
    public void activateShield() {
        this.shieldActive = true;
    }

    @Override
    public void deactivateShield() {
        this.shieldActive = false;
    }

    @Override
    public void applySpeedMultiplier(final double factor, final long durationMs) {
        this.speedMultiplier = factor;
        this.multiplierEndTime = System.nanoTime() + durationMs * 1_000_000;
    }

    @Override
    public double getEffectiveSpeed() {
        if (speedMultiplier != 1.0 && System.nanoTime() >= multiplierEndTime) {
            speedMultiplier = 1.0;
            multiplierEndTime = 0;
        }
        return stats.getBaseSpeed() * speedMultiplier;
    }
}
