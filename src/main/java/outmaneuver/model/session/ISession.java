package outmaneuver.model.session;

public interface ISession {

    int getScore();

    void setScore(int score);

    int getStarsScore();

    void setStarsScore(int starsScore);

    int getMissilesScore();

    void setMissilesScore(int missilesScore);

    int getStars();

    void setStars(int stars);

    double getSpeedMultiplier();

    void setSpeedMultiplier(double speedMultiplier);

    boolean isShieldActive();

    void setShieldActive(boolean shieldActive);

    long getElapsedMs();

    void setElapsedMs(long elapsedMs);

    void reset();
}
