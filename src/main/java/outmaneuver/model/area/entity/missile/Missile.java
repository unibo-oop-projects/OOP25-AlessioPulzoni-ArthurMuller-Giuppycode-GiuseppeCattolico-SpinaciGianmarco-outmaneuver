package outmaneuver.model.area.entity.missile;

import java.awt.Dimension;
import java.util.List;

import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.util.Vector2;

/** Contract for a missile that chases or otherwise moves against the player's plane. */
public interface Missile extends Entity {

    // --- UPDATE E MOVIMENTO ---
    /**
     * Advances the missile's state, steering towards the plane and moving it.
     *
     * @param plane the plane being targeted
     * @param dt elapsed time in seconds since the last update
     */
    void update(Plane plane, double dt);

    /**
     * Redirects the missile towards the plane's predicted position if it has left the
     * playable area.
     *
     * @param plane the plane being targeted
     * @param screenSize the size of the visible play area
     * @param effectiveSpeed the plane's current effective speed, used to predict its position
     */
    void redirectIfOutOfBounds(Plane plane, Dimension screenSize, double effectiveSpeed);

    /**
     * Sets the missile's initial heading towards the given target point.
     *
     * @param target the point to aim the missile at
     */
    void setInitialDirection(Vector2 target);

    // --- STATO ---
    /**
     * Returns whether the missile is still active in the game area.
     *
     * @return {@code true} if the missile has not been destroyed
     */
    boolean isAlive();

    // --- COLLISIONE ---
    /**
     * Notifies the missile that it has collided with the plane, applying its specific
     * collision behaviour (e.g. destroying itself or affecting other missiles).
     *
     * @param activeMissiles the missiles currently active in the game area
     */
    void onCollision(List<Missile> activeMissiles);

    /**
     * Checks whether the missile should bounce off the play area bounds and updates its
     * position and velocity accordingly.
     *
     * @param planePos the plane's current position
     * @param screenSize the size of the visible play area
     */
    void checkBounce(Vector2 planePos, Dimension screenSize);

    // --- EFFETTI ---
    /**
     * Applies a temporary slow-down effect to the missile.
     *
     * @param factor the speed multiplier applied while slowed (e.g. 0.5 for half speed)
     * @param duration how long the slow effect lasts, in seconds
     */
    void slowDown(double factor, double duration);

    // --- RENDER ---
    /**
     * Returns the identifier of this missile's type, used e.g. for rendering.
     *
     * @return the missile type identifier
     */
    String getMissileType();

    /**
     * Returns the direction the missile is currently facing.
     *
     * @return the heading angle in radians
     */
    double getDirection();
}
