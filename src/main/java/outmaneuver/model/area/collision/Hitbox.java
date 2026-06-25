package outmaneuver.model.area.collision;

import outmaneuver.util.Vector2;

import java.util.Objects;

/** Simple circular hitbox. */
public final class Hitbox {
    private final Vector2 center;
    private final double radius;

    /**
     * Creates a circular hitbox.
     *
     * @param center the center of the hitbox in world coordinates
     * @param radius the radius of the hitbox, must be non-negative
     */
    public Hitbox(final Vector2 center, final double radius) {
        this.center = Objects.requireNonNull(center, "center");
        if (radius < 0) {
            throw new IllegalArgumentException("radius must be >= 0");
        }
        this.radius = radius;
    }

    /**
     * Returns the center of this hitbox.
     *
     * @return the center point in world coordinates
     */
    public Vector2 getCenter() {
        return center;
    }

    /**
     * Returns the radius of this hitbox.
     *
     * @return the radius
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Checks whether this hitbox overlaps another circular hitbox.
     *
     * @param other the other hitbox to test against
     * @return {@code true} if the two circles overlap or touch
     */
    public boolean intersects(final Hitbox other) {
        final double dx = center.getX() - other.center.getX();
        final double dy = center.getY() - other.center.getY();
        final double distSq = dx * dx + dy * dy;
        final double r = this.radius + other.radius;
        return distSq <= r * r;
    }

    /**
     * Approximate collision point as midpoint between overlapping circle centers projected
     * towards the intersection region.
     *
     * @param other the other hitbox this one is colliding with
     * @return the approximate collision point in world coordinates
     */
    public Vector2 collisionPoint(final Hitbox other) {
        // midpoint between centers is a reasonable approximation
        final double mx = (center.getX() + other.center.getX()) / 2.0;
        final double my = (center.getY() + other.center.getY()) / 2.0;
        return new Vector2(mx, my);
    }
}
