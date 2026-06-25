package outmaneuver.controller.impl;

import java.util.ArrayList;
import java.util.List;

import outmaneuver.controller.RenderStateAssembler;

import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.collectibles.Collectible;
import outmaneuver.model.area.entity.missile.Missile;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.util.Vector2;
import outmaneuver.view.EntityRenderData;
import outmaneuver.view.HudSnapshot;
import outmaneuver.view.RenderState;

/**
 * Default {@link RenderStateAssembler} implementation. Converts the live entity list
 * into immutable render data per entity category and tracks short-lived explosion
 * animations triggered by collision points across frames.
 */
public final class RenderStateAssemblerImpl implements RenderStateAssembler {

    private static final int EXPLOSION_LIFETIME_TICKS = 12;
    private final List<ExplosionInstance> activeExplosions = new ArrayList<>();

    @Override
    public RenderState assemble(final List<Entity> entities, final boolean paused,
            final long elapsedMs, final int stars, final double speedMultiplier,
            final boolean shieldActive, final List<Vector2> collisionPoints) {
        return RenderState.builder()
                .planeData(buildPlaneData(entities))
                .hud(buildHud(entities, paused, elapsedMs, stars, speedMultiplier, shieldActive))
                .missiles(buildMissileData(entities))
                .collectibles(buildCollectibleData(entities))
                .collisions(buildCollisionData(collisionPoints))
                .build();
    }

    @Override
    public void reset() {
        activeExplosions.clear();
    }

    private EntityRenderData buildPlaneData(final List<Entity> entities) {
        return entities.stream()
                .filter(e -> e instanceof Plane)
                .map(e -> (Plane) e)
                .findFirst()
                .map(p -> new EntityRenderData(
                        p.getPosition().getX(),
                        p.getPosition().getY(),
                        p.getDirection(),
                        p.getStats().getSpriteId(),
                        p.getHitbox().getRadius()))
                .orElse(null);
    }

    private List<EntityRenderData> buildCollectibleData(final List<Entity> entities) {
        return entities.stream()
                .filter(e -> e instanceof Collectible)
                .map(e -> (Collectible) e)
                .map(c -> new EntityRenderData(
                        c.getPosition().getX(),
                        c.getPosition().getY(),
                        0, // i collectible non hanno orientamento
                        //AGGIUNTO: nome completo dello sprite, la view fa solo fromFilename (niente switch)
                        "collectible_" + c.getCollectibleType(),
                        c.getHitbox().getRadius()))
                .toList();
    }

    private List<EntityRenderData> buildMissileData(final List<Entity> entities) {
        return entities.stream()
                .filter(e -> e instanceof Missile)
                .map(e -> (Missile) e)
                .map(m -> new EntityRenderData(
                        m.getPosition().getX(),
                        m.getPosition().getY(),
                        m.getDirection(),
                        // AGGIUNTO: passo il NOME completo dello sprite, cosi' la view
                        // non deve piu' tradurre il tipo (niente switch)
                        "missile_" + m.getMissileType(),
                        m.getHitbox().getRadius()))
                .toList();
    }

    private HudSnapshot buildHud(final List<Entity> entities, final boolean paused,
            final long elapsedMs, final int stars, final double speedMultiplier,
            final boolean shieldActive) {
        final double speed = entities.stream()
                .filter(e -> e instanceof Plane)
                .map(e -> (Plane) e)
                .findFirst()
                .map(p -> p.getStats().getBaseSpeed() * speedMultiplier)
                .orElse(0.0);
        return new HudSnapshot(elapsedMs, speed, shieldActive, paused, stars);
    }

    private List<EntityRenderData> buildCollisionData(final List<Vector2> collisionPoints) {
        for (final var point : collisionPoints) {
            activeExplosions.add(new ExplosionInstance(point.getX(), point.getY(), 0));
        }
        final List<EntityRenderData> result = new ArrayList<>();
        final var iterator = activeExplosions.listIterator();
        while (iterator.hasNext()) {
            final var inst = iterator.next();
            if (inst.tick >= EXPLOSION_LIFETIME_TICKS) {
                iterator.remove();
            } else {
                result.add(new EntityRenderData(
                        inst.x, inst.y,
                        (double) inst.tick,
                        "explosion",
                        1.0));
                iterator.set(new ExplosionInstance(inst.x, inst.y, inst.tick + 1));
            }
        }
        return result;
    }

    private record ExplosionInstance(double x, double y, int tick) {
    }
}
