package outmaneuver.util;

import java.util.Objects;

public final class Vector2 {

    public static final Vector2 ZERO = new Vector2(0, 0);

    private final double x;
    private final double y;

    public Vector2(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    public static Vector2 fromAngle(final double radians) {
        return new Vector2(Math.cos(radians), Math.sin(radians));
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Vector2 add(final Vector2 other) {
        return new Vector2(this.x + other.x, this.y + other.y);
    }

    // [Alessio - missili] aggiunto per steering e posizione relativa al bersaglio
    /** Vettore che va da {@code other} a questo: {@code this - other}. */
    public Vector2 subtract(final Vector2 other) {
        return new Vector2(this.x - other.x, this.y - other.y);
    }

    public Vector2 scale(final double factor) {
        return new Vector2(this.x * factor, this.y * factor);
    }

    public Vector2 normalize() {
        final double mag = magnitude();
        if (mag == 0) {
            return ZERO;
        }
        return new Vector2(this.x / mag, this.y / mag);
    }

    public double magnitude() {
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }

    public double dot(final Vector2 other) {
        return this.x * other.x + this.y * other.y;
    }

    public double angle() {
        return Math.atan2(this.y, this.x);
    }

    /** Normalises an angle in radians to the range [-π, π]. */
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
    /** Inverte la componente orizzontale (rimbalzo su un bordo verticale). */
    public Vector2 reflectX() {
        return new Vector2(-this.x, this.y);
    }

    /** Inverte la componente verticale (rimbalzo su un bordo orizzontale). */
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
