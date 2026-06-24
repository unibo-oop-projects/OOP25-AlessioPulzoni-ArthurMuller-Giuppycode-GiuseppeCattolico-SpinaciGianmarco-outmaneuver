package outmaneuver.model.area.entity.plane;


import outmaneuver.model.area.entity.Entity;

public interface Plane extends Entity {

    double getDirection();

    void setDirection(double direction);

    PlaneStats getStats();

    void setStats(PlaneStats stats);

    TurnState getTurnState();

    void setTurnState(TurnState state);
}
