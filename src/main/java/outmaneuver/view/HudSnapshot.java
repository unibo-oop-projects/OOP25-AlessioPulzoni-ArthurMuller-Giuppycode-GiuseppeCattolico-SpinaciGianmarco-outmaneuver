package outmaneuver.view;

public record HudSnapshot(
        long elapsedMs,
        double speed,
        boolean shieldActive,
        boolean paused,
        int stars
) {
}
