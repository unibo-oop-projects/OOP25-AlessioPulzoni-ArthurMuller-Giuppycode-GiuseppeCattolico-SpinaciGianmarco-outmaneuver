package outmaneuver.controller.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import outmaneuver.controller.CollisionEngine;
import outmaneuver.controller.event.EffectEvent;
import outmaneuver.model.area.effect.Effect;
import outmaneuver.model.area.effect.EffectImpl;
import outmaneuver.model.area.effect.EffectType;
import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.collectibles.Collectible;
import outmaneuver.model.area.entity.collectibles.ShieldPowerUp;
import outmaneuver.model.area.entity.collectibles.SpeedBoost;
import outmaneuver.model.area.entity.collectibles.StarCollectible;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.util.Vector2;

public final class CollectibleControllerImpl extends EntityControllerImpl {

    private static final long SPAWN_INTERVAL_MS = 3000;

    private final Random random = new Random();
    private final List<Effect> activeEffects = new ArrayList<>();
    private long accumulatedMs;

    public CollectibleControllerImpl(
            final List<Entity> entities,
            final CollisionEngine collisionEngine) {
        super(entities, collisionEngine);
    }

    @Override
    public void updateEntities(final long deltaMs) {
        tickSpawn(deltaMs);
        tickEffect(deltaMs);
    }

    public void addEffect(final Effect effect) {
        for (int i = 0; i < activeEffects.size(); i++) {
            if (activeEffects.get(i).getType() == effect.getType()) {
                activeEffects.set(i, effect);
                onInternalEvent(EffectEvent.EFFECT_APPLIED, effect);
                return;
            }
        }
        activeEffects.add(effect);
        onInternalEvent(EffectEvent.EFFECT_APPLIED, effect);
    }

    public boolean hasEffect(final Class<? extends Effect> type) {
        return activeEffects.stream().anyMatch(e -> type.isInstance(e));
    }

    public double getEffectMultiplier() {
        return activeEffects.stream()
                .mapToDouble(Effect::getMultiplier)
                .filter(m -> m > 0.0)
                .findFirst()
                .orElse(1.0);
    }

    @Override
    public void clearAll() {
        for (final Effect effect : activeEffects) {
            onInternalEvent(EffectEvent.EFFECT_EXPIRED, effect);
        }
        activeEffects.clear();
    }

    private void tickEffect(final long deltaMs) {
        final Iterator<Effect> it = activeEffects.iterator();
        while (it.hasNext()) {
            final Effect effect = it.next();
            effect.update(deltaMs);
            if (!effect.isActive()) {
                onInternalEvent(EffectEvent.EFFECT_EXPIRED, effect);
                it.remove();
            }
        }
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
            case 0 -> new StarCollectible(pos, 10);
            case 1 -> new SpeedBoost(pos, new EffectImpl(EffectType.SPEED_BOOST, 2.0, 3000L));
            default -> new ShieldPowerUp(pos, new EffectImpl(EffectType.SHIELD, 5000L));
        };
    }
}
