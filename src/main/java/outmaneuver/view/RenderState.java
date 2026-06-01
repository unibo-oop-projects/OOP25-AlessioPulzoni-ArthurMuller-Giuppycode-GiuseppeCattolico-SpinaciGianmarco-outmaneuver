package outmaneuver.view;

import outmaneuver.model.area.Plane;
import outmaneuver.model.area.TurnState;

public final class RenderState {

    private final EntityRenderData plane;

    private RenderState(final EntityRenderData plane) {
        this.plane = plane;
    }

    public EntityRenderData getPlane() {
        return plane;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Plane plane;

        private Builder() {
        }

        public Builder plane(final Plane plane) {
            this.plane = plane;
            return this;
        }

        public RenderState build() {
            final EntityRenderData planeData = new EntityRenderData(
                    plane.getPosition().getX(),
                    plane.getPosition().getY(),
                    plane.getDirection(),
                    plane.getStats().getSpriteId()
            );
            return new RenderState(planeData);
        }
    }
}
