package outmaneuver.model.session;

public final class Session implements ISession {

    private int score;
    private int starsScore;
    private int missilesScore;
    private int stars;
    private double speedMultiplier;
    private boolean shieldActive;
    private long elapsedMs;

    public Session() {
        reset();
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
    public int getStars() {
        return stars;
    }

    @Override
    public void setStars(final int stars) {
        this.stars = stars;
    }

    @Override
    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    @Override
    public void setSpeedMultiplier(final double speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    @Override
    public boolean isShieldActive() {
        return shieldActive;
    }

    @Override
    public void setShieldActive(final boolean shieldActive) {
        this.shieldActive = shieldActive;
    }

    @Override
    public long getElapsedMs() {
        return elapsedMs;
    }

    @Override
    public void addElapsed(final long ms) {
        this.elapsedMs += ms;
    }

    @Override
    public void increaseStars() {
        this.stars += 1;
    }

    @Override
    public void reset() {
        this.score = 0;
        this.starsScore = 0;
        this.missilesScore = 0;
        this.stars = 0;
        this.speedMultiplier = 1.0;
        this.shieldActive = false;
        this.elapsedMs = 0;
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
