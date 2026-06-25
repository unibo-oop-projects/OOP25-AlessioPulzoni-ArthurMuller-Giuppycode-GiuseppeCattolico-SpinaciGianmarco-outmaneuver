package outmaneuver.util;

import java.util.Objects;

/**
 * Immutable two-dimensional vector with the usual arithmetic and geometric operations
 * used to represent positions, velocities and directions in the game world.
 */
public final class Vector2 {

    public static final Vector2 ZERO = new Vector2(0, 0);

    private final double x;
    private final double y;

    /**
     * Creates a vector with the given components.
     *
     * @param x the horizontal component
     * @param y the vertical component
     */
    public Vector2(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Builds a unit vector pointing in the direction of the given angle.
     *
     * @param radians the direction, in radians
     * @return a unit vector with that direction
     */
    public static Vector2 fromAngle(final double radians) {
        return new Vector2(Math.cos(radians), Math.sin(radians));
    }

    /**
     * Returns the horizontal component.
     *
     * @return the x component
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the vertical component.
     *
     * @return the y component
     */
    public double getY() {
        return y;
    }

    /**
     * Adds another vector to this one.
     *
     * @param other the vector to add
     * @return a new vector equal to the component-wise sum
     */
    public Vector2 add(final Vector2 other) {
        return new Vector2(this.x + other.x, this.y + other.y);
    }

    // [Alessio - missili] aggiunto per steering e posizione relativa al bersaglio
    /**
     * Vettore che va da {@code other} a questo: {@code this - other}.
     *
     * @param other il vettore di partenza
     * @return il vettore differenza {@code this - other}
     */
    public Vector2 subtract(final Vector2 other) {
        return new Vector2(this.x - other.x, this.y - other.y);
    }

    /**
     * Scales this vector by a scalar factor.
     *
     * @param factor the scaling factor
     * @return a new vector equal to this one scaled by {@code factor}
     */
    public Vector2 scale(final double factor) {
        return new Vector2(this.x * factor, this.y * factor);
    }

    /**
     * Returns a unit vector with the same direction as this one.
     *
     * @return the normalised vector, or {@link #ZERO} if this vector has zero magnitude
     */
    public Vector2 normalize() {
        final double mag = magnitude();
        if (mag == 0) {
            return ZERO;
        }
        return new Vector2(this.x / mag, this.y / mag);
    }

    /**
     * Returns the length of this vector.
     *
     * @return the Euclidean magnitude of this vector
     */
    public double magnitude() {
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }

    /**
     * Computes the dot product with another vector.
     *
     * @param other the other vector
     * @return the dot product of this vector and {@code other}
     */
    public double dot(final Vector2 other) {
        return this.x * other.x + this.y * other.y;
    }

    /**
     * Returns the direction of this vector.
     *
     * @return the angle of this vector, in radians
     */
    public double angle() {
        return Math.atan2(this.y, this.x);
    }

    /**
     * Normalises an angle in radians to the range [-π, π].
     *
     * @param angle the angle to normalise, in radians
     * @return the equivalent angle within [-π, π]
     */
    public static double normaliseAngle(final double angle) {
        final double twoPi = 2 * Math.PI;
        double normalised = angle % twoPi;
        if (normalised > Math.PI) {
            normalised -= twoPi;
        } else if (normalised < -Math.PI) {
            normalised += twoPi;
        }
        return normalised;
    }

    // [Alessio - missili] reflectX/reflectY aggiunti per il rimbalzo del BounceMissile
    /**
     * Inverte la componente orizzontale (rimbalzo su un bordo verticale).
     *
     * @return un nuovo vettore con la componente x invertita
     */
    public Vector2 reflectX() {
        return new Vector2(-this.x, this.y);
    }

    /**
     * Inverte la componente verticale (rimbalzo su un bordo orizzontale).
     *
     * @return un nuovo vettore con la componente y invertita
     */
    public Vector2 reflectY() {
        return new Vector2(this.x, -this.y);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof final Vector2 vector2)) {
            return false;
        }
        return Double.compare(vector2.x, x) == 0 && Double.compare(vector2.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Vector2(" + x + ", " + y + ")";
    }
}
