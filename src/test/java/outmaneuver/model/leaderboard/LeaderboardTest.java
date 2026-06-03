package outmaneuver.model.leaderboard;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import outmaneuver.model.session.ScoreEntry;

class LeaderboardTest {

    @TempDir
    Path tempDir;

    private ILeaderboardRepository repository;
    private Leaderboard leaderboard;

    @BeforeEach
    void setUp() {
        repository = new JsonLeaderboardRepository(tempDir.resolve("scores.json"));
        leaderboard = new Leaderboard(repository, 3);
    }

    @Test
    void emptyLeaderboardReturnsEmptyList() {
        assertTrue(leaderboard.getTopScores().isEmpty());
    }

    @Test
    void savedScoreIsRetrievable() {
        leaderboard.save(100, "Alice");
        assertEquals(1, leaderboard.getTopScores().size());
        assertEquals(100, leaderboard.getTopScores().get(0).score());
    }

    @Test
    void scoresAreSortedDescending() {
        leaderboard.save(50, "Bob");
        leaderboard.save(200, "Alice");
        leaderboard.save(100, "Charlie");

        final List<ScoreEntry> top = leaderboard.getTopScores();
        assertEquals(200, top.get(0).score());
        assertEquals(100, top.get(1).score());
        assertEquals(50, top.get(2).score());
    }

    @Test
    void onlyTopNEntriesAreKept() {
        leaderboard.save(10, "D");
        leaderboard.save(20, "C");
        leaderboard.save(30, "B");
        leaderboard.save(40, "A");

        assertEquals(3, leaderboard.getTopScores().size());
        assertEquals(40, leaderboard.getTopScores().get(0).score());
    }

    @Test
    void persistedDataSurvivesReload() {
        leaderboard.save(500, "Alice");
        final Leaderboard reloaded = new Leaderboard(repository, 3);
        assertEquals(1, reloaded.getTopScores().size());
        assertEquals("Alice", reloaded.getTopScores().get(0).playerName());
    }

    @Test
    void saveThrowsOnNullPlayerName() {
        assertThrows(NullPointerException.class, () -> leaderboard.save(100, null));
    }

    @Test
    void constructorThrowsOnNullRepository() {
        assertThrows(NullPointerException.class, () -> new Leaderboard(null));
    }

    @Test
    void constructorThrowsOnZeroMaxEntries() {
        assertThrows(IllegalArgumentException.class,
                () -> new Leaderboard(repository, 0));
    }

    @Test
    void persistThrowsOnNullEntries() {
        assertThrows(NullPointerException.class, () -> repository.persist(null));
    }

    @Test
    void topScoresListIsUnmodifiable() {
        leaderboard.save(100, "Alice");
        assertThrows(UnsupportedOperationException.class,
                () -> leaderboard.getTopScores().add(new ScoreEntry(1, "X", java.time.LocalDate.now())));
    }
}
