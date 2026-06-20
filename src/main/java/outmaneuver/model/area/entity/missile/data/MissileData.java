package outmaneuver.model.area.entity.missile.data;

import java.util.Objects;

/*
 * Parametri di un missile, caricati dal JSON.
 * I campi sono comuni a tutti i tipi; l'effetto di rallentamento ({@link SlowEffect})
 * e' opzionale (null) e lo usa solo il clock.
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
     * Rallentamento che un missile applica agli altri quando collide (solo il clock).
     *
     * @param factor   moltiplicatore di velocita' (0..1) durante l'effetto
     * @param duration durata in secondi
     */
    public record SlowEffect(double factor, double duration) { }
}
