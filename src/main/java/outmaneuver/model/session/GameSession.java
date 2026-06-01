package outmaneuver.model.session;

import java.util.Objects;

import outmaneuver.controller.event.GameEventBus;
import outmaneuver.model.plane.IPlane;

public class GameSession implements IGameSession {

    private final GameEventBus eventBus;
    private final long startTimeMs;
    private final int score;
    private GameState currentState;
    private IPlane plane;

    public GameSession(final GameEventBus eventBus) {
        this.eventBus = Objects.requireNonNull(eventBus);
        this.startTimeMs = System.currentTimeMillis();
        this.score = 0;
        this.currentState = GameState.MENU;
    }

    @Override
    public GameState getGameState() {
        return currentState;
    }

    @Override
    public int getScore() {
        return score;
    }

    @Override
    public long getElapsedTimeMillis() {
        return System.currentTimeMillis() - startTimeMs;
    }

    @Override
    public void equipPlane(final IPlane plane) {
        this.plane = Objects.requireNonNull(plane);
    }
}
