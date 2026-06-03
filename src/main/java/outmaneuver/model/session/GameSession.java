package outmaneuver.model.session;

import java.util.Objects;

import outmaneuver.model.area.Plane;

public final class GameSession implements IGameSession {

    private int score;
    private GameState currentState;
    private Plane plane;

    /** Millisecondi di gioco accumulati prima della pausa corrente. */
    private long accumulatedPlayingMs;
    /** System.currentTimeMillis() all'ultimo ingresso in PLAYING. */
    private long playingStartMs;

    public GameSession() {
        this.score = 0;
        this.currentState = GameState.MENU;
        this.accumulatedPlayingMs = 0;
    }

    @Override
    public GameState getGameState() {
        return currentState;
    }

    @Override
    public int getScore() {
        return score;
    }

    @Override
    public long getElapsedTimeMillis() {
        if (currentState == GameState.PLAYING) {
            return accumulatedPlayingMs + (System.currentTimeMillis() - playingStartMs);
        }
        return accumulatedPlayingMs;
    }

    @Override
    public void equipPlane(final Plane plane) {
        this.plane = Objects.requireNonNull(plane, "plane must not be null");
    }

    @Override
    public void incrementScore(final int delta) {
        if (delta <= 0) {
            throw new IllegalArgumentException("delta must be positive, was: " + delta);
        }
        score += delta;
    }

    @Override
    public void transitionTo(final GameState state) {
        Objects.requireNonNull(state, "state must not be null");
        validateTransition(currentState, state);
        onExit(currentState);
        currentState = state;
        onEnter(state);
    }

    // ------------------------------------------------------------------ //

    private void onExit(final GameState exiting) {
        if (exiting == GameState.PLAYING) {
            accumulatedPlayingMs += System.currentTimeMillis() - playingStartMs;
        }
    }

    private void onEnter(final GameState entering) {
        if (entering == GameState.PLAYING) {
            playingStartMs = System.currentTimeMillis();
        }
    }

    private static void validateTransition(final GameState from, final GameState to) {
        final boolean valid = switch (from) {
            case MENU      -> to == GameState.PLAYING;
            case PLAYING   -> to == GameState.PAUSED || to == GameState.GAME_OVER;
            case PAUSED    -> to == GameState.PLAYING || to == GameState.GAME_OVER;
            case GAME_OVER -> to == GameState.MENU;
        };
        if (!valid) {
            throw new IllegalArgumentException("Invalid transition: " + from + " -> " + to);
        }
    }
}
