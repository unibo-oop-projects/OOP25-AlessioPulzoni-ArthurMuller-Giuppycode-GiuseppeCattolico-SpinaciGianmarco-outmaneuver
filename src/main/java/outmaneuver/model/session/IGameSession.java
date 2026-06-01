package outmaneuver.model.session;

import outmaneuver.model.area.Plane;

public interface IGameSession {

    GameState getGameState();

    int getScore();

    long getElapsedTimeMillis();

    void equipPlane(Plane plane);
}
