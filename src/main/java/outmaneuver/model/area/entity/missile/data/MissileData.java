package outmaneuver.model.area.entity.missile.data;

import java.util.Objects;

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
    public MissileData {
        Objects.requireNonNull(type, "type must not be null");
        if (speed <= 0) {
            throw new IllegalArgumentException("speed must be positive");
        }
        if (radius <= 0) {
            throw new IllegalArgumentException("radius must be positive");
        }
    }

    public record SlowEffect(double factor, double duration) { }
}
