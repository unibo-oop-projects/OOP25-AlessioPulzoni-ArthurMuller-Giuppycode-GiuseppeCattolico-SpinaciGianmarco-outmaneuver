package outmaneuver.view.swing.gameover;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.model.session.ScoreEntry;

class GameOverViewTest {

    private GameOverView view;

    @BeforeEach
    void setUp() {
        view = new GameOverView(() -> { }, () -> { });
    }

    @Test
    void constructorRejectsNullPlayAgain() {
        assertThrows(NullPointerException.class, () -> new GameOverView(null, () -> { }));
    }

    @Test
    void constructorRejectsNullMenu() {
        assertThrows(NullPointerException.class, () -> new GameOverView(() -> { }, null));
    }

    @Test
    void showWithEmptyListDoesNotThrow() {
        assertDoesNotThrow(() -> view.show(0, List.of()));
    }

    @Test
    void showWithScoresDoesNotThrow() {
        final List<ScoreEntry> scores = List.of(
                new ScoreEntry(500, "Alice", LocalDate.of(2026, 6, 1)),
                new ScoreEntry(300, "Bob",   LocalDate.of(2026, 6, 2))
        );
        assertDoesNotThrow(() -> view.show(420, scores));
    }

    @Test
    void showRejectsNullTopScores() {
        assertThrows(NullPointerException.class, () -> view.show(100, null));
    }

    @Test
    void showMoreThanFiveEntriesDoesNotThrow() {
        final List<ScoreEntry> scores = List.of(
                new ScoreEntry(900, "A", LocalDate.now()),
                new ScoreEntry(800, "B", LocalDate.now()),
                new ScoreEntry(700, "C", LocalDate.now()),
                new ScoreEntry(600, "D", LocalDate.now()),
                new ScoreEntry(500, "E", LocalDate.now()),
                new ScoreEntry(400, "F", LocalDate.now())
        );
        assertDoesNotThrow(() -> view.show(350, scores));
    }
}
