package outmaneuver.view;

import java.util.List;

import outmaneuver.model.area.Plane;
import outmaneuver.model.missile.MissileRenderData;

public final class RenderState {

    private final EntityRenderData plane;
    private final List<MissileRenderData> missiles;  // AGGIUNTO

    private RenderState(final EntityRenderData plane,
                        final List<MissileRenderData> missiles) {
        this.plane    = plane;
        this.missiles = missiles;
    }

    public EntityRenderData getPlane() { return plane; }

    public List<MissileRenderData> getMissiles() { return missiles; }  // AGGIUNTO

    public static Builder builder() { return new Builder(); }

    public static final class Builder {

        private Plane plane;
        private List<MissileRenderData> missiles = List.of();  // AGGIUNTO

        private Builder() { }

        public Builder plane(final Plane plane) {
            this.plane = plane;
            return this;
        }

        // AGGIUNTO
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
            return new RenderState(planeData, missiles);
        }
    }
}