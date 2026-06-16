package outmaneuver.model.area.entity.collectibles;

import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.session.IGameSession;
import outmaneuver.util.Vector2;

public final class SpeedBoost extends AbstractCollectible {

    private final double factor;
    private final long durationMs;

    public SpeedBoost(final Vector2 position, final double factor, final long durationMs) {
        super(position); // Initialize position with the provided value
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
