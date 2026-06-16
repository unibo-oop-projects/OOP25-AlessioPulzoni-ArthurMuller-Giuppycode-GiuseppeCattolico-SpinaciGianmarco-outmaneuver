package outmaneuver.model.area.entity.plane;

import outmaneuver.util.Vector2;

public interface Plane {

    Vector2 getPosition();

    void setPosition(Vector2 position);

    double getDirection();

    void setDirection(double direction);

    PlaneStats getStats();

    void setStats(PlaneStats stats);

    TurnState getTurnState();

    void setTurnState(TurnState state);

    boolean isShieldActive();

    void activateShield();

    void deactivateShield();

    void applySpeedMultiplier(double factor, long durationMs);

    double getEffectiveSpeed();
}
