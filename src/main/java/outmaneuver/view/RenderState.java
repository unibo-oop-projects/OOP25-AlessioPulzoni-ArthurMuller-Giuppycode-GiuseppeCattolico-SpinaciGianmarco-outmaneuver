package outmaneuver.view;

import outmaneuver.model.area.entity.plane.Plane;

public final class RenderState {

    private final EntityRenderData plane;
    private final HudSnapshot hud;

    private RenderState(final EntityRenderData plane, final HudSnapshot hud) {
        this.plane = plane;
        this.hud = hud;
    }

    public EntityRenderData getPlane() {
        return plane;
    }

    public HudSnapshot getHud() {
        return hud;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Plane plane;
        private HudSnapshot hud;

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

        public RenderState build() {
            final EntityRenderData planeData = new EntityRenderData(
                    plane.getPosition().getX(),
                    plane.getPosition().getY(),
                    plane.getDirection(),
                    plane.getStats().getSpriteId()
            );
            return new RenderState(planeData, hud);
        }
    }
}
