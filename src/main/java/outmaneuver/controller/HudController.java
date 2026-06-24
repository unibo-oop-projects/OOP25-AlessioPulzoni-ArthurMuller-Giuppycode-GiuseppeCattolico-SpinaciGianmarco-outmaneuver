package outmaneuver.controller;

import outmaneuver.controller.event.InternalEventListener;

public interface HudController extends InternalEventListener {

    void onTick(long deltaMs);

    void setShieldActive(boolean active);

    void setSpeedMultiplier(double multiplier);

    long getElapsedMs();

    double getSpeedMultiplier();

    boolean isShieldActive();

    int getStars();

    void reset();
}
