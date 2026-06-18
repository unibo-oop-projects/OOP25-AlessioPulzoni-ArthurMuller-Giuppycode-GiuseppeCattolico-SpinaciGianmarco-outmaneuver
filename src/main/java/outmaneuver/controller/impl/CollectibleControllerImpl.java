package outmaneuver.controller.impl;


import java.util.List;
import java.util.Random;

import outmaneuver.controller.CollisionEngine;
import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.collectibles.Collectible;
import outmaneuver.model.area.entity.collectibles.ShieldPowerUp;
import outmaneuver.model.area.entity.collectibles.SpeedBoost;
import outmaneuver.model.area.entity.collectibles.StarCollectible;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.session.IGameSession;
import outmaneuver.util.Vector2;

public final class CollectibleControllerImpl extends EntityControllerImpl {

    private static final long SPAWN_INTERVAL_MS = 3000;

    private final Random random = new Random();
    private long accumulatedMs;

    public CollectibleControllerImpl(
            final List<Entity> entities,
            final CollisionEngine collisionEngine,
            final IGameSession session) {
        super(entities, collisionEngine, session);
    }

    @Override
    public void updateEntities(final long deltaMs) {
        tickSpawn(deltaMs);
    }

    private void tickSpawn(final long deltaMs) {
        accumulatedMs += deltaMs;
        if (accumulatedMs < SPAWN_INTERVAL_MS) {
            return;
        }
        final Vector2 spawnPos = randomEdgePosition();
        if (spawnPos == null) {
            return;
        }
        accumulatedMs = 0;
        spawnEntity(randomCollectible(spawnPos));
    }

    private Vector2 randomEdgePosition() {
        final Plane plane = getEntities().stream()
                .filter(e -> e instanceof Plane)
                .map(e -> (Plane) e)
                .findFirst()
                .orElse(null);
        if (getView() == null || plane == null) {
            return null;
        }
        final int w = getView().getWidth();
        final int h = getView().getHeight();
        if (w <= 0 || h <= 0) {
            return null;
        }
        final double cx = plane.getPosition().getX();
        final double cy = plane.getPosition().getY();
        final double hw = w / 2.0;
        final double hh = h / 2.0;
        return switch (random.nextInt(4)) {
            case 0 -> new Vector2(cx + (random.nextDouble() * 2 - 1) * hw, cy - hh); // top
            case 1 -> new Vector2(cx + (random.nextDouble() * 2 - 1) * hw, cy + hh); // bottom
            case 2 -> new Vector2(cx - hw, cy + (random.nextDouble() * 2 - 1) * hh); // left
            default -> new Vector2(cx + hw, cy + (random.nextDouble() * 2 - 1) * hh); // right
        };
    }

    private Collectible randomCollectible(final Vector2 pos) {
        return switch (random.nextInt(3)) {
            case 0  -> new StarCollectible(pos, 10);
            case 1  -> new SpeedBoost(pos, 2.0, 3000L);
            default -> new ShieldPowerUp(pos, 5000L);
        };
    }


}
