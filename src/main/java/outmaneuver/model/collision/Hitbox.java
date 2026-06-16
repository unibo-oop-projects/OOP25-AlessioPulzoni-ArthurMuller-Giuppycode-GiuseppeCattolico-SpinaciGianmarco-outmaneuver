package outmaneuver.model.collision;

import outmaneuver.util.Vector2;

import java.util.Objects;

/** Simple circular hitbox. */
public final class Hitbox {
    private final Vector2 center;
    private final double radius;

    public Hitbox(final Vector2 center, final double radius) {
        this.center = Objects.requireNonNull(center, "center");
        if (radius < 0) throw new IllegalArgumentException("radius must be >= 0");
        this.radius = radius;
    }

    public Vector2 getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }

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
     */
    public Vector2 collisionPoint(final Hitbox other) {
        // midpoint between centers is a reasonable approximation
        final double mx = (center.getX() + other.center.getX()) / 2.0;
        final double my = (center.getY() + other.center.getY()) / 2.0;
        return new Vector2(mx, my);
    }
}
