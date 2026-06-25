package outmaneuver.controller;

/**
 * Tracks raw key press/release input and exposes it as a normalized turning direction
 * for the plane. Implementers must be safe to call from the input event thread while
 * being read from the game loop thread.
 */
public interface InputController {

    /**
     * Records that a key has been pressed.
     *
     * @param keyCode the key code that was pressed
     */
    void onKeyPressed(int keyCode);

    /**
     * Records that a key has been released.
     *
     * @param keyCode the key code that was released
     */
    void onKeyReleased(int keyCode);

    /**
     * Returns the current turning direction derived from the keys currently held down.
     *
     * @return {@code -1.0} for left, {@code 1.0} for right, or {@code 0.0} when no turn
     *     is requested or opposing keys cancel out
     */
    double getTurnDirection();

    /** Clears all tracked key state, e.g. when starting a new game. */
    void reset();
}
