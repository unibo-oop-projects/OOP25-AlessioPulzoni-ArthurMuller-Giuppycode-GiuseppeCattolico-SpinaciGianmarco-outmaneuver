package outmaneuver.model.session;

public interface ISession {

    int getScore();

    int getStarsScore();

    int getMissilesScore();

    void setStars(int stars);

    int getStars();

    void setSpeedMultiplier(double speedMultiplier);

    double getSpeedMultiplier();

    void setShieldActive(boolean shieldActive);

    boolean isShieldActive();

    long getElapsedMs();

    void addElapsed(long ms);

    void increaseStars();

    void reset();

    void incrementScore(int delta);

    void incrementStarsScore(int delta);

    void incrementMissilesScore(int delta);
}
