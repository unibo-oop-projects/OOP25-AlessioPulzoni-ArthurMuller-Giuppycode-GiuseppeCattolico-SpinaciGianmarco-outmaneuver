package outmaneuver.model.area.entity.collectibles;

import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.session.IGameSession;
import outmaneuver.util.Vector2;

public final class ShieldPowerUp extends AbstractCollectible {

    private final long durationMs;

    public ShieldPowerUp(final Vector2 position, final long durationMs) {
        super(position); // Initialize position with the provided value
        if (durationMs <= 0) {
            throw new IllegalArgumentException("durationMs must be positive");
        }
        this.durationMs = durationMs;
    }

    @Override
    public void apply(final Plane plane, final IGameSession session) {
        plane.activateShield();
        Thread.ofVirtual().start(() -> {
            try {
                Thread.sleep(durationMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            plane.deactivateShield();
        });
    }
}
