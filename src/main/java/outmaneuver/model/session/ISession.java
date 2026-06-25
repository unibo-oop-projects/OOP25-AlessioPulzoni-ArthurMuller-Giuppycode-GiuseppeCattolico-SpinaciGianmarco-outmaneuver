package outmaneuver.model.session;

/**
 * Holds the mutable state of a single play session: score, stars, power-up
 * multipliers and elapsed time. Implementations are plain data holders;
 * the rules for how the state changes live in the controllers that use it.
 */
public interface ISession {

    /**
     * Returns the current score.
     *
     * @return the score accumulated so far in this session
     */
    int getScore();

    /**
     * Sets the current score.
     *
     * @param score the new score value
     */
    void setScore(int score);

    /**
     * Returns the portion of the score earned through stars.
     *
     * @return the stars score accumulated so far in this session
     */
    int getStarsScore();

    /**
     * Sets the portion of the score earned through stars.
     *
     * @param starsScore the new stars score value
     */
    void setStarsScore(int starsScore);

    /**
     * Returns the portion of the score earned by destroying missiles.
     *
     * @return the missiles score accumulated so far in this session
     */
    int getMissilesScore();

    /**
     * Sets the portion of the score earned by destroying missiles.
     *
     * @param missilesScore the new missiles score value
     */
    void setMissilesScore(int missilesScore);

    /**
     * Returns the number of stars collected.
     *
     * @return the star count for this session
     */
    int getStars();

    /**
     * Sets the number of stars collected.
     *
     * @param stars the new star count
     */
    void setStars(int stars);

    /**
     * Returns the current speed multiplier applied to the plane.
     *
     * @return the active speed multiplier
     */
    double getSpeedMultiplier();

    /**
     * Sets the speed multiplier applied to the plane.
     *
     * @param speedMultiplier the new speed multiplier
     */
    void setSpeedMultiplier(double speedMultiplier);

    /**
     * Tells whether the shield power-up is currently active.
     *
     * @return {@code true} if the shield is active, {@code false} otherwise
     */
    boolean isShieldActive();

    /**
     * Activates or deactivates the shield power-up.
     *
     * @param shieldActive {@code true} to activate the shield, {@code false} to deactivate it
     */
    void setShieldActive(boolean shieldActive);

    /**
     * Returns how much game time has elapsed in this session.
     *
     * @return the elapsed time, in milliseconds
     */
    long getElapsedMs();

    /**
     * Sets how much game time has elapsed in this session.
     *
     * @param elapsedMs the new elapsed time, in milliseconds
     */
    void setElapsedMs(long elapsedMs);

    /**
     * Resets the session to its initial state (zero score, no stars, no power-ups, no elapsed time).
     */
    void reset();
}
