package outmaneuver.controller.impl;

import java.util.List;

import outmaneuver.controller.RenderStateAssembler;

import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.collectibles.Collectible;
import outmaneuver.model.area.entity.missile.Missile;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.view.EntityRenderData;
import outmaneuver.view.HudSnapshot;
import outmaneuver.view.RenderState;

public final class RenderStateAssemblerImpl implements RenderStateAssembler {

    public RenderStateAssemblerImpl() {
    }

    @Override
    public RenderState assemble(final List<Entity> entities, final boolean paused,
            final long elapsedMs, final int stars,
            final double speedMultiplier, final boolean shieldActive) {
        final EntityRenderData planeData = buildPlaneData(entities);
        final List<EntityRenderData> collectibles = buildCollectibleData(entities);
        final List<EntityRenderData> missiles = buildMissileData(entities);
        final double speed = computeSpeed(entities, speedMultiplier);
        final HudSnapshot hud = new HudSnapshot(elapsedMs, speed, shieldActive, paused, stars);
        return RenderState.builder()
                .planeData(planeData)
                .hud(hud)
                .missiles(missiles)
                .collectibles(collectibles)
                .build();
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
                        p.getStats().getSpriteId()))
                .orElse(null);
    }

    private List<EntityRenderData> buildCollectibleData(final List<Entity> entities) {
        return entities.stream()
                .filter(e -> e instanceof Collectible)
                .map(e -> new EntityRenderData(
                        e.getPosition().getX(),
                        e.getPosition().getY(),
                        0,
                        "collectible"))
                .toList();
    }

    private double computeSpeed(final List<Entity> entities, final double speedMultiplier) {
        return entities.stream()
                .filter(e -> e instanceof Plane)
                .map(e -> (Plane) e)
                .findFirst()
                .map(p -> p.getStats().getBaseSpeed() * speedMultiplier)
                .orElse(0.0);
    }

    private List<EntityRenderData> buildMissileData(final List<Entity> entities) {
        return entities.stream()
                .filter(e -> e instanceof Missile)
                .map(e -> (Missile) e)
                .map(m -> new EntityRenderData(
                        m.getPosition().getX(),
                        m.getPosition().getY(),
                        m.getDirection(),
                        m.getMissileType()))
                .toList();
    }
}
