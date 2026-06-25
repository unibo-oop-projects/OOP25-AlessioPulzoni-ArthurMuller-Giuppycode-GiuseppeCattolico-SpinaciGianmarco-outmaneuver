package outmaneuver.view;

import java.util.List;

/**
 * Immutable snapshot of everything a {@link GameView} needs to render a single frame:
 * the player's plane, the HUD data, and the currently visible missiles, collectibles
 * and collision effects. Built via {@link #builder()}.
 */
public final class RenderState {

    private final EntityRenderData plane;
    private final HudSnapshot hud;
    // [Alessio - missili] lista dei dati di render dei missili
    private final List<EntityRenderData> missiles;
    private final List<EntityRenderData> collectibles;
    private final List<EntityRenderData> collisions;

    private RenderState(final EntityRenderData plane,
                        final HudSnapshot hud,
                        final List<EntityRenderData> missiles,
                        final List<EntityRenderData> collectibles,
                        final List<EntityRenderData> collisions) {
        this.plane = plane;
        this.hud = hud;
        this.missiles = List.copyOf(missiles);
        this.collectibles = List.copyOf(collectibles);
        this.collisions = List.copyOf(collisions);
    }

    /**
     * Returns the render data for the player's plane.
     *
     * @return the plane's render data
     */
    public EntityRenderData getPlane() {
        return plane;
    }

    /**
     * Returns the render data for all missiles currently visible in the scene.
     *
     * @return an immutable copy of the missiles' render data
     */
    public List<EntityRenderData> getMissiles() {
        return List.copyOf(missiles);
    }

    /**
     * Returns the HUD data for this frame.
     *
     * @return the HUD snapshot
     */
    public HudSnapshot getHud() {
        return hud;
    }

    /**
     * Returns the render data for all collectibles currently visible in the scene.
     *
     * @return an immutable copy of the collectibles' render data
     */
    public List<EntityRenderData> getCollectibles() {
        return List.copyOf(collectibles);
    }

    /**
     * Returns the render data for all active collision/explosion effects.
     *
     * @return an immutable copy of the collisions' render data
     */
    public List<EntityRenderData> getCollisions() {
        return List.copyOf(collisions);
    }

    /**
     * Creates a new builder for assembling a {@link RenderState}.
     *
     * @return a fresh, empty builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Mutable builder used to assemble an immutable {@link RenderState}. */
    public static final class Builder {

        private EntityRenderData planeData;
        private HudSnapshot hud;
        private List<EntityRenderData> missiles = List.of();
        private List<EntityRenderData> collectibles = List.of();
        private List<EntityRenderData> collisions = List.of();

        private Builder() { }

        /**
         * Sets the plane's render data.
         *
         * @param value the plane's render data
         * @return this builder, for chaining
         */
        public Builder planeData(final EntityRenderData value) {
            this.planeData = value;
            return this;
        }

        /**
         * Sets the HUD data.
         *
         * @param value the HUD snapshot
         * @return this builder, for chaining
         */
        public Builder hud(final HudSnapshot value) {
            this.hud = value;
            return this;
        }

        /**
         * Sets the missiles' render data.
         *
         * @param value the missiles' render data
         * @return this builder, for chaining
         */
        public Builder missiles(final List<EntityRenderData> value) {
            this.missiles = List.copyOf(value);
            return this;
        }

        /**
         * Sets the collectibles' render data.
         *
         * @param value the collectibles' render data
         * @return this builder, for chaining
         */
        public Builder collectibles(final List<EntityRenderData> value) {
            this.collectibles = List.copyOf(value);
            return this;
        }

        /**
         * Sets the collisions' render data.
         *
         * @param value the collisions' render data
         * @return this builder, for chaining
         */
        public Builder collisions(final List<EntityRenderData> value) {
            this.collisions = List.copyOf(value);
            return this;
        }

        /**
         * Builds the immutable {@link RenderState} from the values set so far.
         *
         * @return the assembled render state
         */
        public RenderState build() {
            return new RenderState(planeData, hud, missiles, collectibles, collisions);
        }
    }
}
