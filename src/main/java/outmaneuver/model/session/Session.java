package outmaneuver.model.session;

/** Default {@link ISession} implementation: a plain, mutable holder of session state. */
public final class Session implements ISession {

    private int score;
    private int starsScore;
    private int missilesScore;
    private int stars;
    private double speedMultiplier;
    private boolean shieldActive;
    private long elapsedMs;

    /** Creates a new session, already reset to its initial state. */
    public Session() {
        reset();
    }

    @Override
    public int getScore() {
        return score;
    }

    @Override
    public void setScore(final int score) {
        this.score = score;
    }

    @Override
    public int getStarsScore() {
        return starsScore;
    }

    @Override
    public void setStarsScore(final int starsScore) {
        this.starsScore = starsScore;
    }

    @Override
    public int getMissilesScore() {
        return missilesScore;
    }

    @Override
    public void setMissilesScore(final int missilesScore) {
        this.missilesScore = missilesScore;
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
    public void setElapsedMs(final long elapsedMs) {
        this.elapsedMs = elapsedMs;
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
}
