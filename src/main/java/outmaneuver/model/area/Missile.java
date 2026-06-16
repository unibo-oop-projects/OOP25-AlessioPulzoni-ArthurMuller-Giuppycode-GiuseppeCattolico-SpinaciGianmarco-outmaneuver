package outmaneuver.model.area;

import outmaneuver.model.Entity;

/**
 * Interface for missile entities.
 */
public interface Missile extends Entity {

    /**
     * Gets the speed of the missile.
     *
     * @return the speed value
     */
    double getSpeed();

    /**
     * Sets the speed of the missile.
     *
     * @param speed the new speed
     */
    void setSpeed(double speed);

    /**
     * Gets the current direction of the missile, in radians.
     *
     * @return the direction value
     */
    double getDirection();

    /**
     * Sets the current direction of the missile, in radians.
     *
     * @param direction the new direction
     */
    void setDirection(double direction);

    void update(long deltaMs);
}
