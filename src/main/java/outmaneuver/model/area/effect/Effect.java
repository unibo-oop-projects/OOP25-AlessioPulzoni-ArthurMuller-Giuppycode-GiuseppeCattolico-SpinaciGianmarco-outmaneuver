package outmaneuver.model.area.effect;

/** A timed gameplay effect (e.g. shield, speed boost) that can be applied to an entity. */
public interface Effect {

    /**
     * Advances the effect's remaining duration by the given time step.
     *
     * @param deltaMs elapsed time in milliseconds since the last update
     */
    void update(long deltaMs);

    /**
     * Returns whether the effect is still in effect.
     *
     * @return {@code true} if the effect has not yet expired
     */
    boolean isActive();

    /**
     * Returns the multiplier applied by this effect (e.g. to speed).
     *
     * @return the effect's multiplier value
     */
    double getMultiplier();

    /**
     * Returns the kind of effect.
     *
     * @return the effect type
     */
    EffectType getType();
}
