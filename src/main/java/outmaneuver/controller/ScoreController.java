package outmaneuver.controller;

import outmaneuver.controller.event.InternalEventListener;

public interface ScoreController extends InternalEventListener {

    void onTick();

    void onTick(long deltaMs);

    void reset();

    int getScore();

    long getElapsedMs();

    int getStars();

    void increaseStars();

    double getSpeedMultiplier();

    void setSpeedMultiplier(double speedMultiplier);

    boolean isShieldActive();

    void setShieldActive(boolean shieldActive);
}
