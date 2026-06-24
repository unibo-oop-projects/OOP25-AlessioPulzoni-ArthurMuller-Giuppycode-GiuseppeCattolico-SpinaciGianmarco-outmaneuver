package outmaneuver.model.area.effect;

public final class EffectImpl implements Effect {

    private final EffectType type;
    private long remainingMs;
    private boolean active;
    private double multiplier;

    public EffectImpl(final EffectType type, final double multiplier, final long durationMs) {
        this.type = type;
        this.remainingMs = durationMs;
        this.multiplier = multiplier;
        this.active = true;
    }

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
