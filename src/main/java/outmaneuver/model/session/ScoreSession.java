package outmaneuver.model.session;


public final class ScoreSession implements IScoreSession {

    private int score;


    public ScoreSession() {
        this.score = 0;
    }

    @Override
    public int getScore() {
        return score;
    }

    @Override
    public void reset() {
        this.score = 0;

    }

    @Override
    public void incrementScore(final int delta) {
        if (delta <= 0) {
            throw new IllegalArgumentException("delta must be positive, was: " + delta);
        }
        score += delta;
    }
}
