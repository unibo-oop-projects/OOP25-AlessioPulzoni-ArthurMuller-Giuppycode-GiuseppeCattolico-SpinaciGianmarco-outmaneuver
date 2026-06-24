package outmaneuver.model.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScoreSessionTest {

    private ScoreSession session;

    @BeforeEach
    void setUp() {
        session = new ScoreSession();
    }

    @Test
    void initialScoreIsZero() {
        assertEquals(0, session.getScore());
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
