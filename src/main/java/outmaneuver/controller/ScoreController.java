package outmaneuver.controller;

import outmaneuver.controller.event.InternalEventListener;

/**
 * Tracks the current game session's score, elapsed time, stars and active modifiers,
 * and reacts to gameplay events (collisions) that affect them. Implementers must keep
 * the score, elapsed time and modifiers consistent across ticks and resets.
 */
public interface ScoreController extends InternalEventListener {

    /** Advances the score/time bookkeeping by one game tick, using the controller's own tick duration. */
    void onTick();

    /**
     * Advances the score/time bookkeeping by the given amount of elapsed time.
     *
     * @param deltaMs milliseconds elapsed since the previous tick
     */
    void onTick(long deltaMs);

    /** Resets score, elapsed time, stars and modifiers to their initial values for a new game. */
    void reset();

    /**
     * Returns the current score accumulated in this session.
     *
     * @return the current score
     */
    int getScore();

    /**
     * Returns the total time elapsed in the current session.
     *
     * @return elapsed time in milliseconds
     */
    long getElapsedMs();

    /**
     * Returns the number of stars collected in the current session.
     *
     * @return the current star count
     */
    int getStars();

    /** Increments the star count by one, typically in response to collecting a star. */
    void increaseStars();

    /**
     * Returns the multiplier currently applied to gameplay speed.
     *
     * @return the active speed multiplier
     */
    double getSpeedMultiplier();

    /**
     * Sets the multiplier applied to gameplay speed, e.g. from a speed boost effect.
     *
     * @param speedMultiplier the new speed multiplier
     */
    void setSpeedMultiplier(double speedMultiplier);

    /**
     * Returns whether the shield effect is currently active for the player.
     *
     * @return {@code true} if the shield is active
     */
    boolean isShieldActive();

    /**
     * Sets whether the shield effect is currently active for the player.
     *
     * @param shieldActive {@code true} to activate the shield, {@code false} to deactivate it
     */
    void setShieldActive(boolean shieldActive);
}
