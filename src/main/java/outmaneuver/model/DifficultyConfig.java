package outmaneuver.model;

public record DifficultyConfig(
        double initialSpawnRate,
        double spawnRateIncrement,
        int maxMissilesOnScreen
) {
    public DifficultyConfig {
        if (initialSpawnRate <= 0 || spawnRateIncrement <= 0 || maxMissilesOnScreen <= 0) {
            throw new IllegalArgumentException("All values must be positive");
        }
    }

    public static DifficultyConfig defaultConfig() {
        return new DifficultyConfig(1.5, 0.1, 5);
    }
}
