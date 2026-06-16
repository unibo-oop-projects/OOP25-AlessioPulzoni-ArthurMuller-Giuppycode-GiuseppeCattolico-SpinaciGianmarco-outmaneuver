package outmaneuver.model.profile;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PlayerProfileTest {

    private PlayerProfile profile;

    @BeforeEach
    void setUp(@TempDir final Path tmpDir) {
        profile = new PlayerProfile(JsonPlayerProfileRepository.create(tmpDir.resolve("profile.json")));
    }

    // ── default state ──

    @Test
    void defaultCoinsIsZero() {
        assertEquals(0, profile.getCoins());
    }

    @Test
    void defaultOwnsStandardPlane() {
        assertTrue(profile.ownsPlane("standard"));
    }

    @Test
    void defaultDoesNotOwnUnknownPlane() {
        assertFalse(profile.ownsPlane("fast"));
    }

    @Test
    void defaultTopScoresIsEmpty() {
        assertTrue(profile.getTopScores().isEmpty());
    }

    // ── wallet ──

    @Test
    void addCoinsAccumulates() {
        profile.addCoins(300);
        profile.addCoins(200);
        assertEquals(500, profile.getCoins());
    }

    @Test
    void addCoinsRejectsZero() {
        assertThrows(IllegalArgumentException.class, () -> profile.addCoins(0));
    }

    @Test
    void spendReturnsTrueAndDeducts() {
        profile.addCoins(500);
        assertTrue(profile.spend(200));
        assertEquals(300, profile.getCoins());
    }

    @Test
    void spendReturnsFalseWhenInsufficient() {
        profile.addCoins(100);
        assertFalse(profile.spend(200));
        assertEquals(100, profile.getCoins());
    }

    // ── owned planes ──

    @Test
    void addOwnedPlaneTracksNewPlane() {
        profile.addOwnedPlane("fast");
        assertTrue(profile.ownsPlane("fast"));
    }

    @Test
    void addOwnedPlaneDuplicateIsIdempotent() {
        profile.addOwnedPlane("fast");
        profile.addOwnedPlane("fast");
        assertTrue(profile.ownsPlane("fast"));
    }

    // ── scores ──

    @Test
    void saveScoreAppearsInTopScores() {
        profile.saveScore(300, "Alice");
        assertEquals(1, profile.getTopScores().size());
        assertEquals(300, profile.getTopScores().get(0).score());
    }

    @Test
    void topScoresAreSortedDescending() {
        profile.saveScore(100, "A");
        profile.saveScore(400, "B");
        profile.saveScore(200, "C");
        assertEquals(400, profile.getTopScores().get(0).score());
        assertEquals(100, profile.getTopScores().get(2).score());
    }

    @Test
    void topScoresTrimmedToTen() {
        for (int i = 1; i <= 15; i++) {
            profile.saveScore(i * 10, "Player");
        }
        assertEquals(10, profile.getTopScores().size());
        assertEquals(150, profile.getTopScores().get(0).score());
    }

    @Test
    void topScoresListIsUnmodifiable() {
        profile.saveScore(100, "A");
        assertThrows(UnsupportedOperationException.class,
                () -> profile.getTopScores().clear());
    }

    // ── persistence ──

    @Test
    void profilePersistsAcrossInstances(@TempDir final Path tmpDir) {
        final Path file = tmpDir.resolve("profile.json");
        final PlayerProfile p1 = new PlayerProfile(JsonPlayerProfileRepository.create(file));
        p1.addCoins(250);
        p1.addOwnedPlane("heavy");
        p1.saveScore(500, "Bob");

        final PlayerProfile p2 = new PlayerProfile(JsonPlayerProfileRepository.create(file));
        assertEquals(250, p2.getCoins());
        assertTrue(p2.ownsPlane("heavy"));
        assertEquals(500, p2.getTopScores().get(0).score());
    }
}
