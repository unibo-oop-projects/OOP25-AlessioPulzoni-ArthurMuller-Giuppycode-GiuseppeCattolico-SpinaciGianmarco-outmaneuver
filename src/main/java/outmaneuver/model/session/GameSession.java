package outmaneuver.model.session;


public final class GameSession implements IGameSession {

    private int score;
    private GameState currentState;

    private long accumulatedPlayingMs;
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
    public void reset() {
        this.score = 0;
        this.currentState = GameState.MENU;
        this.accumulatedPlayingMs = 0;
        this.playingStartMs = 0;
    }

    @Override
    public void incrementScore(final int delta) {
        if (delta <= 0) {
            throw new IllegalArgumentException("delta must be positive, was: " + delta);
        }
        score += delta;
    }
}
