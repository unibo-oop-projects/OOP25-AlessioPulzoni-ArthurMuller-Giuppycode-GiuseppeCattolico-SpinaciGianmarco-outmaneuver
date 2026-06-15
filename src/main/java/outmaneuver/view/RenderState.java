package outmaneuver.view;

import java.util.List;

import outmaneuver.model.area.Plane;
public final class RenderState {

    private final EntityRenderData plane;
    private final HudSnapshot hud;
    private final List<MissileRenderData> missiles;

    private RenderState(final EntityRenderData plane,
                        final HudSnapshot hud,
                        final List<MissileRenderData> missiles) {
        this.plane    = plane;
        this.hud      = hud;
        this.missiles = missiles;
    }

    public EntityRenderData getPlane() { return plane; }

    public HudSnapshot getHud() { return hud; }

    public List<MissileRenderData> getMissiles() { return missiles; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {

        private Plane plane;
        private HudSnapshot hud;
        private List<MissileRenderData> missiles = List.of();

        private Builder() { }

        public Builder plane(final Plane plane) {
            this.plane = plane;
            return this;
        }

        public Builder hud(final HudSnapshot hud) {
            this.hud = hud;
            return this;
        }

        public Builder missiles(final List<MissileRenderData> missiles) {
            this.missiles = missiles;
            return this;
        }

        public RenderState build() {
            final EntityRenderData planeData = new EntityRenderData(
                    plane.getPosition().getX(),
                    plane.getPosition().getY(),
                    plane.getDirection(),
                    plane.getStats().getSpriteId()
            );
            return new RenderState(planeData, hud, missiles);
        }
    }
}