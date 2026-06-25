package outmaneuver.model.profile;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PlayerProfileTest {

    private static final String FAST_PLANE_ID = "fast";

    private PlayerProfile profile;

    @BeforeEach
    void setUp(@TempDir final Path tmpDir) {
        final Path file = tmpDir.resolve("profile.json");
        final var repo = JsonPlayerProfileRepository.create(file);
        repo.persist(new PlayerProfileData("", 0, List.of("standard"), List.of()));
        profile = new PlayerProfile(repo);
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
        assertFalse(profile.ownsPlane(FAST_PLANE_ID));
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
        profile.addOwnedPlane(FAST_PLANE_ID);
        assertTrue(profile.ownsPlane(FAST_PLANE_ID));
    }

    @Test
    void addOwnedPlaneDuplicateIsIdempotent() {
        profile.addOwnedPlane(FAST_PLANE_ID);
        profile.addOwnedPlane(FAST_PLANE_ID);
        assertTrue(profile.ownsPlane(FAST_PLANE_ID));
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
        assertEquals(15, profile.getTopScores().size());
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
        final var repo = JsonPlayerProfileRepository.create(file);
        repo.persist(new PlayerProfileData("", 0, List.of("standard"), List.of()));
        final PlayerProfile p1 = new PlayerProfile(repo);
        p1.addCoins(250);
        p1.addOwnedPlane("heavy");
        p1.saveScore(500, "Bob");

        final PlayerProfile p2 = new PlayerProfile(JsonPlayerProfileRepository.create(file));
        assertEquals(250, p2.getCoins());
        assertTrue(p2.ownsPlane("heavy"));
        assertEquals(500, p2.getTopScores().get(0).score());
    }
}
