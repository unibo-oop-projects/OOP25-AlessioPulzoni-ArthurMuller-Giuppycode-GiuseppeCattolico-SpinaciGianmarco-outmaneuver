package outmaneuver.view;

import outmaneuver.model.area.entity.plane.Plane;
import java.util.List;


public final class RenderState {

    private final EntityRenderData plane;
    private final HudSnapshot hud;
    private final List<EntityRenderData> collectibles;

    private RenderState(final EntityRenderData plane, final HudSnapshot hud,
                        final List<EntityRenderData> collectibles) {
        this.plane = plane;
        this.hud = hud;
        this.collectibles = collectibles;
    }

    public EntityRenderData getPlane() {
        return plane;
    }

    public HudSnapshot getHud() {
        return hud;
    }

    public List<EntityRenderData> getCollectibles() {
        return collectibles;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Plane plane;
        private HudSnapshot hud;
        private List<EntityRenderData> collectibles = List.of();

        private Builder() {
        }

        public Builder plane(final Plane plane) {
            this.plane = plane;
            return this;
        }

        public Builder hud(final HudSnapshot hud) {
            this.hud = hud;
            return this;
        }

        public Builder collectibles(final List<EntityRenderData> collectibles) {
            this.collectibles = collectibles;
            return this;
        }

        public RenderState build() {
            final EntityRenderData planeData = new EntityRenderData(
                    plane.getPosition().getX(),
                    plane.getPosition().getY(),
                    plane.getDirection(),
                    plane.getStats().getSpriteId()
            );
            return new RenderState(planeData, hud, collectibles);
        }
    }
}
