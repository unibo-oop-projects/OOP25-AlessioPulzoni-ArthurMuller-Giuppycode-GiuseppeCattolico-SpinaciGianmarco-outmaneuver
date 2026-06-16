package outmaneuver.controller.impl;

import java.util.Random;

import outmaneuver.controller.EntityController;
import outmaneuver.model.area.Plane;
import outmaneuver.model.collectibles.Collectible;
import outmaneuver.model.collectibles.ShieldPowerUp;
import outmaneuver.model.collectibles.SpeedBoost;
import outmaneuver.model.collectibles.StarCollectible;
import outmaneuver.util.Vector2;

public final class CollectibleSpawner {

    private static final long SPAWN_INTERVAL_MS = 3000;
    private static final double MIN_DIST = 200;
    private static final double MAX_DIST = 500;

    private final EntityController entityController;
    private final Random random = new Random();
    private long accumulatedMs;

    public CollectibleSpawner(final EntityController entityController) {
        this.entityController = entityController;
    }

    public void tick(final long deltaMs, final Plane plane) {
        accumulatedMs += deltaMs;
        if (accumulatedMs < SPAWN_INTERVAL_MS) {
            return;
        }
        accumulatedMs = 0;
        entityController.spawnCollectible(randomCollectible(randomPositionAround(plane.getPosition())));
    }

    private Vector2 randomPositionAround(final Vector2 center) {
        final double angle = random.nextDouble() * 2 * Math.PI;
        final double dist = MIN_DIST + random.nextDouble() * (MAX_DIST - MIN_DIST);
        return new Vector2(center.getX() + Math.cos(angle) * dist,
                           center.getY() + Math.sin(angle) * dist);
    }

    private Collectible randomCollectible(final Vector2 pos) {
        return switch (random.nextInt(3)) {
            case 0  -> new StarCollectible(pos, 10);
            case 1  -> new SpeedBoost(pos, 2.0, 3000L);
            default -> new ShieldPowerUp(pos, 5000L);
        };
    }
}
