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

}
