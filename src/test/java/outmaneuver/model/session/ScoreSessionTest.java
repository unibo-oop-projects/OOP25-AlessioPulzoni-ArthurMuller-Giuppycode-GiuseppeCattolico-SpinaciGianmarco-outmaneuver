package outmaneuver.model.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScoreSessionTest {

    private ISession session;

    @BeforeEach
    void setUp() {
        session = new Session();
    }

    @Test
    void initialScoreIsZero() {
        assertEquals(0, session.getScore());
    }

}
