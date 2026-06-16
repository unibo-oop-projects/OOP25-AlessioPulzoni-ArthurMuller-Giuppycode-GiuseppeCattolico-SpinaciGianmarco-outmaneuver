package outmaneuver.model.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameSessionTest {

    private GameSession session;

    @BeforeEach
    void setUp() {
        session = new GameSession();
    }

    @Test
    void initialStateIsMenu() {
        assertEquals(GameState.MENU, session.getGameState());
    }

    @Test
    void initialScoreIsZero() {
        assertEquals(0, session.getScore());
    }

    @Test
    void elapsedTimeIsZeroBeforePlaying() {
        assertEquals(0, session.getElapsedTimeMillis());
    }

    @Test
    void incrementScoreAddsCorrectly() {
        session.incrementScore(10);
        session.incrementScore(5);
        assertEquals(15, session.getScore());
    }

    @Test
    void incrementScoreThrowsOnNonPositiveDelta() {
        assertThrows(IllegalArgumentException.class, () -> session.incrementScore(0));
        assertThrows(IllegalArgumentException.class, () -> session.incrementScore(-1));
    }

    @Test
    void transitionMenuToPlaying() {
        session.transitionTo(GameState.PLAYING);
        assertEquals(GameState.PLAYING, session.getGameState());
    }

    @Test
    void transitionPlayingToPaused() {
        session.transitionTo(GameState.PLAYING);
        session.transitionTo(GameState.PAUSED);
        assertEquals(GameState.PAUSED, session.getGameState());
    }

    @Test
    void transitionPlayingToGameOver() {
        session.transitionTo(GameState.PLAYING);
        session.transitionTo(GameState.GAME_OVER);
        assertEquals(GameState.GAME_OVER, session.getGameState());
    }

    @Test
    void transitionGameOverToMenu() {
        session.transitionTo(GameState.PLAYING);
        session.transitionTo(GameState.GAME_OVER);
        session.transitionTo(GameState.MENU);
        assertEquals(GameState.MENU, session.getGameState());
    }

    @Test
    void invalidTransitionMenuToPausedThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> session.transitionTo(GameState.PAUSED));
    }

    @Test
    void invalidTransitionMenuToGameOverThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> session.transitionTo(GameState.GAME_OVER));
    }

    @Test
    void invalidTransitionPlayingToMenuThrows() {
        session.transitionTo(GameState.PLAYING);
        assertThrows(IllegalArgumentException.class,
                () -> session.transitionTo(GameState.MENU));
    }

    @Test
    void elapsedTimeGrowsWhilePlaying() throws InterruptedException {
        session.transitionTo(GameState.PLAYING);
        Thread.sleep(50);
        assertTrue(session.getElapsedTimeMillis() >= 40);
    }

    @Test
    void elapsedTimeFrozenWhilePaused() throws InterruptedException {
        session.transitionTo(GameState.PLAYING);
        Thread.sleep(30);
        session.transitionTo(GameState.PAUSED);
        final long frozenMs = session.getElapsedTimeMillis();
        Thread.sleep(30);
        assertEquals(frozenMs, session.getElapsedTimeMillis());
    }

    @Test
    void elapsedTimeAccumulatesAcrossPauses() throws InterruptedException {
        session.transitionTo(GameState.PLAYING);
        Thread.sleep(30);
        session.transitionTo(GameState.PAUSED);
        final long afterFirstPlaying = session.getElapsedTimeMillis();
        session.transitionTo(GameState.PLAYING);
        Thread.sleep(30);
        assertTrue(session.getElapsedTimeMillis() > afterFirstPlaying);
    }

    @Test
    void transitionToNullThrows() {
        assertThrows(NullPointerException.class, () -> session.transitionTo(null));
    }
}
