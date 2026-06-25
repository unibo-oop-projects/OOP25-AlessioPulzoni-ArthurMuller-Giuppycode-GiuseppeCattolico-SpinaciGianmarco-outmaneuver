package outmaneuver.model.session;


public final class ScoreSession implements IScoreSession {

    private int score;
    private int starsScore;
    private int missilesScore;

    public ScoreSession() {
        this.score = 0;
        this.starsScore = 0;
        this.missilesScore = 0;
    }

    @Override
    public int getScore() {
        return score;
    }

    @Override
    public int getStarsScore() {
        return starsScore;
    }

    @Override
    public int getMissilesScore() {
        return missilesScore;
    }

    @Override
    public void reset() {
        this.score = 0;
        this.starsScore = 0;
        this.missilesScore = 0;
    }

    @Override
    public void incrementScore(final int delta) {
        if (delta <= 0) {
            throw new IllegalArgumentException("delta must be positive, was: " + delta);
        }
        score += delta;
    }

    @Override
    public void incrementStarsScore(final int delta) {
        if (delta <= 0) {
            throw new IllegalArgumentException("delta must be positive, was: " + delta);
        }
        starsScore += delta;
    }

    @Override
    public void incrementMissilesScore(final int delta) {
        if (delta <= 0) {
            throw new IllegalArgumentException("delta must be positive, was: " + delta);
        }
        missilesScore += delta;
    }
}
