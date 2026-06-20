package outmaneuver.controller.impl;

import java.util.List;
import java.util.Objects;

import outmaneuver.controller.HudController;
import outmaneuver.controller.RenderStateAssembler;

import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.collectibles.Collectible;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.view.EntityRenderData;
import outmaneuver.view.HudSnapshot;
import outmaneuver.view.RenderState;

public final class RenderStateAssemblerImpl implements RenderStateAssembler {

    private final HudController hudController;

    public RenderStateAssemblerImpl(final HudController hudController) {
        this.hudController = Objects.requireNonNull(hudController, "hudController must not be null");
    }

    @Override
    public RenderState assemble(final List<Entity> entities, final boolean paused) {
        final EntityRenderData planeData = buildPlaneData(entities);
        final List<EntityRenderData> collectibles = buildCollectibleData(entities);
        final HudSnapshot hud = buildHud(entities, paused);
        return RenderState.builder()
                .planeData(planeData)
                .hud(hud)
                .collectibles(collectibles)
                .build();
    }

    @Override
    public void reset() {
        hudController.reset();
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

    private HudSnapshot buildHud(final List<Entity> entities, final boolean paused) {
        final Plane plane = entities.stream()
                .filter(e -> e instanceof Plane)
                .map(e -> (Plane) e)
                .findFirst()
                .orElse(null);
        return hudController.buildSnapshot(plane, paused);
    }
}
