package outmaneuver.model.area.effect;

/** Default {@link Effect} implementation with a fixed duration and optional multiplier. */
public final class EffectImpl implements Effect {

    private final EffectType type;
    private long remainingMs;
    private boolean active;
    private double multiplier;

    /**
     * Creates an active effect with an explicit multiplier.
     *
     * @param type the kind of effect
     * @param multiplier the multiplier applied while the effect is active
     * @param durationMs how long the effect lasts, in milliseconds
     */
    public EffectImpl(final EffectType type, final double multiplier, final long durationMs) {
        this.type = type;
        this.remainingMs = durationMs;
        this.multiplier = multiplier;
        this.active = true;
    }

    /**
     * Creates an active effect with no multiplier.
     *
     * @param type the kind of effect
     * @param durationMs how long the effect lasts, in milliseconds
     */
    public EffectImpl(final EffectType type, final long durationMs) {
        this.type = type;
        this.remainingMs = durationMs;
        this.active = true;
    }

    @Override
    public void update(final long deltaMs) {
        remainingMs -= deltaMs;
        if (remainingMs <= 0) {
            this.active = false;
        }
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public double getMultiplier() {
        return this.multiplier;
    }

    @Override
    public EffectType getType() {
        return type;
    }
}
