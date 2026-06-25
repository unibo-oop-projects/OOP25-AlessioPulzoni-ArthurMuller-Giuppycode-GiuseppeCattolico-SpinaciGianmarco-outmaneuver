package outmaneuver.controller.event;

/** High-level game state transitions handled by a {@link outmaneuver.controller.MasterController}. */
public enum GameEvent implements Event {
    RUNNING,
    PAUSED,
    QUIT_APPLICATION,
    GAME_OVER
}
