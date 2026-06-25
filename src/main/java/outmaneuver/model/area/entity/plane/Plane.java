package outmaneuver.model.area.entity.plane;

import outmaneuver.model.area.entity.Entity;

/** Contract for the player-controlled plane: heading, stats, turning state and movement. */
public interface Plane extends Entity {

    /**
     * Returns the direction the plane is currently facing.
     *
     * @return the heading angle in radians
     */
    double getDirection();

    /**
     * Sets the direction the plane is facing.
     *
     * @param direction the new heading angle in radians
     */
    void setDirection(double direction);

    /**
     * Returns the plane's current stats (speed, turn rate, hitbox, etc.).
     *
     * @return the plane's stats
     */
    PlaneStats getStats();

    /**
     * Sets the plane's stats, e.g. after the player switches to a different plane.
     *
     * @param stats the new stats to apply
     */
    void setStats(PlaneStats stats);

    /**
     * Returns the plane's current turning state.
     *
     * @return the current turn state
     */
    TurnState getTurnState();

    /**
     * Sets the plane's turning state.
     *
     * @param state the new turn state
     */
    void setTurnState(TurnState state);

    /**
     * Advances the plane's heading and position based on player input.
     *
     * @param deltaSec elapsed time in seconds since the last update
     * @param turnInput the turning input, negative for left, positive for right, zero for none
     * @param speedMultiplier the multiplier applied to the plane's base speed (e.g. from effects)
     */
    void update(double deltaSec, double turnInput, double speedMultiplier);

    /** Resets the plane to its initial position, direction and turn state. */
    void reset();
}
