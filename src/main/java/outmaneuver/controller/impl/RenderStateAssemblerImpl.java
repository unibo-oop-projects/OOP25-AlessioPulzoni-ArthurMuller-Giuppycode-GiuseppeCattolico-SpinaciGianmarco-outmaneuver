package outmaneuver.controller.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import outmaneuver.controller.HudController;
import outmaneuver.controller.RenderStateAssembler;

import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.collectibles.Collectible;
import outmaneuver.model.area.entity.missile.Missile;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.util.Vector2;
import outmaneuver.view.EntityRenderData;
import outmaneuver.view.HudSnapshot;
import outmaneuver.view.RenderState;

public final class RenderStateAssemblerImpl implements RenderStateAssembler {

    private static final int EXPLOSION_LIFETIME_TICKS = 12;

    private final HudController hudController;
    private final List<Vector2> pendingCollisionPoints = new ArrayList<>();
    private final List<ExplosionInstance> activeExplosions = new ArrayList<>();

    public RenderStateAssemblerImpl(final HudController hudController) {
        this.hudController = Objects.requireNonNull(hudController, "hudController must not be null");
    }

    @Override
    public RenderState assemble(final List<Entity> entities, final boolean paused) {
        final EntityRenderData planeData = buildPlaneData(entities);
        final List<EntityRenderData> collectibles = buildCollectibleData(entities);
        final List<EntityRenderData> missiles = buildMissileData(entities);
        final HudSnapshot hud = buildHud(entities, paused);
        final List<EntityRenderData> collisions = buildCollisionData();
        return RenderState.builder()
                .planeData(planeData)
                .hud(hud)
                .missiles(missiles)
                .collectibles(collectibles)
                .collisions(collisions)
                .build();
    }

    @Override
    public void feedCollisionPoint(final Vector2 point) {
        pendingCollisionPoints.add(point);
    }

    @Override
    public void reset() {
        hudController.reset();
        pendingCollisionPoints.clear();
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
                        0,                        // i collectible non hanno orientamento
                        c.getCollectibleType(),   // tipo di dominio, come getMissileType()
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
                        m.getMissileType(),
                        m.getHitbox().getRadius()))
                .toList();
    }

    private HudSnapshot buildHud(final List<Entity> entities, final boolean paused) {
        final Plane plane = entities.stream()
                .filter(e -> e instanceof Plane)
                .map(e -> (Plane) e)
                .findFirst()
                .orElse(null); 
        return hudController.buildSnapshot(plane, paused);
    }

    private List<EntityRenderData> buildCollisionData() {
        for (final var point : pendingCollisionPoints) {
            activeExplosions.add(new ExplosionInstance(point.getX(), point.getY(), 0));
        }
        pendingCollisionPoints.clear();
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

    private record ExplosionInstance(double x, double y, int tick) {}
}
