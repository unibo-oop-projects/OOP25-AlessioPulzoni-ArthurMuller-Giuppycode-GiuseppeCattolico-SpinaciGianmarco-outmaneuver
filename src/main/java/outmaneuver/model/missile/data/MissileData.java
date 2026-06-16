package outmaneuver.model.missile.data;

import java.util.Objects;

/*
 * Record con i parametri di un tipo di missile — caricato da JSON.
 * Analogo a PlaneData per i piani.
 */
public record MissileData(
        String type,
        double speed,
        double maxTurn,
        double radius,
        double lifetime
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
}