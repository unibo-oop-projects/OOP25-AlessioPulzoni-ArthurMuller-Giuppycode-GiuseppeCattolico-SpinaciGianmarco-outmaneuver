package outmaneuver.model.session;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class ScoreEntryTest {

    private static final LocalDate DATE = LocalDate.of(2026, 6, 1);

    @Test
    void higherScoreComesFirst() {
        final var high = new ScoreEntry(100, "Alice", DATE);
        final var low  = new ScoreEntry(50,  "Bob",   DATE);
        assertTrue(high.compareTo(low) < 0, "higher score should sort before lower score");
    }

    @Test
    void lowerScoreComesLast() {
        final var high = new ScoreEntry(100, "Alice", DATE);
        final var low  = new ScoreEntry(50,  "Bob",   DATE);
        assertTrue(low.compareTo(high) > 0, "lower score should sort after higher score");
    }

    @Test
    void equalScoresCompareToZero() {
        final var a = new ScoreEntry(75, "Alice", DATE);
        final var b = new ScoreEntry(75, "Bob",   DATE);
        assertEquals(0, a.compareTo(b));
    }

    @Test
    void nullPlayerNameThrows() {
        assertThrows(NullPointerException.class, () -> new ScoreEntry(10, null, DATE));
    }

    @Test
    void nullDateThrows() {
        assertThrows(NullPointerException.class, () -> new ScoreEntry(10, "Alice", null));
    }
}
