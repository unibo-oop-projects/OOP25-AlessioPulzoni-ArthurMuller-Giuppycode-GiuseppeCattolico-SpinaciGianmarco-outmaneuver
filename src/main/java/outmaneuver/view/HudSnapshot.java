package outmaneuver.view;

/**
 * Immutable snapshot of the data the HUD needs to draw itself for a single frame.
 *
 * @param elapsedMs the elapsed game time, in milliseconds
 * @param speed the plane's current speed
 * @param shieldActive whether the plane's shield is currently active
 * @param paused whether the game is currently paused
 * @param stars the number of stars collected so far
 */
public record HudSnapshot(
        long elapsedMs,
        double speed,
        boolean shieldActive,
        boolean paused,
        int stars
) {
}
