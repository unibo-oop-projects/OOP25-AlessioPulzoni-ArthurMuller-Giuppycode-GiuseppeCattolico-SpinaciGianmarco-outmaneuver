package outmaneuver.model.session;

import outmaneuver.model.plane.IPlane;

public interface IGameSession {

    /** Returns the current state of the game. */
    GameState getGameState();

    /** Returns the current score accumulated in this session. */
    int getScore();

    /** Returns the milliseconds elapsed since the session started. */
    long getElapsedTimeMillis();

    /** Equips the given plane as the player's active aircraft. */
    void equipPlane(IPlane plane);
}
