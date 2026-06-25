package outmaneuver.model.area.entity.plane;

/** The movement and appearance characteristics that define a plane type. */
public interface PlaneStats {

    /**
     * Returns the plane type identifier.
     *
     * @return the plane type identifier
     */
    String getId();

    /**
     * Returns the plane's base movement speed.
     *
     * @return the base speed, in world units per second
     */
    double getBaseSpeed();

    /**
     * Returns the plane's turning speed.
     *
     * @return the turn rate, in radians per second
     */
    double getTurnRate();

    /**
     * Returns the plane's hitbox radius.
     *
     * @return the hitbox radius
     */
    double getHitboxRadius();

    /**
     * Returns the identifier of the sprite used to render this plane.
     *
     * @return the sprite identifier
     */
    String getSpriteId();
}
