package outmaneuver.model.collectibles;

import outmaneuver.model.area.Plane;
import outmaneuver.model.session.IGameSession;

public final class ShieldPowerUp implements Collectible {

    private final long durationMs;

    public ShieldPowerUp(final long durationMs) {
        if (durationMs <= 0) {
            throw new IllegalArgumentException("durationMs must be positive");
        }
        this.durationMs = durationMs;
    }

    @Override
    public void apply(final Plane plane, final IGameSession session) {
        plane.activateShield();
        final Thread deactivator = Thread.ofVirtual().start(() -> {
            try {
                Thread.sleep(durationMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            plane.deactivateShield();
        });
    }
}
