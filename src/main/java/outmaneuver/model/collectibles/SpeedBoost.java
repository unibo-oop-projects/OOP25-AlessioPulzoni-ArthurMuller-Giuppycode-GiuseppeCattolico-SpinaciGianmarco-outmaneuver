package outmaneuver.model.collectibles;

import outmaneuver.model.area.Plane;
import outmaneuver.model.session.IGameSession;

public final class SpeedBoost implements Collectible {

    private final double factor;
    private final long durationMs;

    public SpeedBoost(final double factor, final long durationMs) {
        if (factor <= 0) {
            throw new IllegalArgumentException("factor must be positive");
        }
        if (durationMs <= 0) {
            throw new IllegalArgumentException("durationMs must be positive");
        }
        this.factor = factor;
        this.durationMs = durationMs;
    }

    @Override
    public void apply(final Plane plane, final IGameSession session) {
        plane.applySpeedMultiplier(factor, durationMs);
    }
}
