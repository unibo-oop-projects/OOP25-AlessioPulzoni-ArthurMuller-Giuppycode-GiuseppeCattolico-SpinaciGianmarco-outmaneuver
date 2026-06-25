package outmaneuver.model.area.entity.missile.data;

import java.util.Objects;

/**
 * Immutable definition of a missile type, loaded from JSON, describing its movement,
 * lifetime and optional slow effect.
 *
 * @param type the missile type identifier
 * @param speed the movement speed, in world units per second
 * @param maxTurn the maximum turn angle the missile can steer per update, in radians
 * @param radius the hitbox radius
 * @param lifetime how long the missile stays alive, in seconds (negative means unlimited)
 * @param predictionTime how far ahead the plane's position is predicted when redirecting
 *     an out-of-bounds missile
 * @param outOfBoundsMargin the extra margin, in pixels, beyond the screen edge before the
 *     missile is considered out of bounds
 * @param slow the slow effect applied by this missile on collision, or {@code null} if none
 */
public record MissileData(
        String type,
        double speed,
        double maxTurn,
        double radius,
        double lifetime,
        double predictionTime,
        int outOfBoundsMargin,
        SlowEffect slow
) {
    /**
     * Validates the missile definition's invariants.
     */
    public MissileData {
        Objects.requireNonNull(type, "type must not be null");
        if (speed <= 0) {
            throw new IllegalArgumentException("speed must be positive");
        }
        if (radius <= 0) {
            throw new IllegalArgumentException("radius must be positive");
        }
    }

    /**
     * A slowing effect applied to other missiles, e.g. by a clock missile.
     *
     * @param factor the speed multiplier applied while slowed (e.g. 0.5 for half speed)
     * @param duration how long the slow effect lasts, in seconds
     */
    public record SlowEffect(double factor, double duration) { }
}
