package outmaneuver.controller;

import outmaneuver.controller.event.InternalEventListener;
import outmaneuver.view.GameView;
import outmaneuver.controller.event.GameEvent;

/**
 * Top-level orchestrator contract for the game: owns the game state transitions
 * driven by {@link GameEvent}s, runs the main game loop, and dispatches frames to
 * attached views.
 */
public interface MasterController extends InternalEventListener {

    /**
     * Reacts to a high-level game state event (pause/resume, quit, game over).
     *
     * @param event the event describing the requested state transition
     */
    void handleEvent(GameEvent event);

    /**
     * Registers a view that will receive rendered frames produced by the game loop.
     *
     * @param view the view to attach
     */
    void attachView(GameView view);

    /** Starts (or resumes) the game loop, transitioning the game into the running state. */
    void start();

    /** Stops the game loop without releasing the underlying thread resources. */
    void stop();

    /** Stops the game loop and releases the underlying thread, leaving the controller unusable afterwards. */
    void shutdown();
}
