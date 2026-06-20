package outmaneuver.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.model.area.entity.missile.Missile;

/**
 * Verifica le regole di selezione del tipo di missile: sblocco per tempo,
 * vincolo di "minimo missili attivi" (il clock) e robustezza dell'estrazione.
 * Usa un generatore con seed fisso per essere deterministico.
 */
class MissileSpawnDirectorTest {

    private static final int RUNS = 200;

    private MissileSpawnDirector director;

    @BeforeEach
    void setUp() {
        director = new MissileSpawnDirector(new Random(42));
    }

    @Test
    void atStartOnlyBasicIsUnlocked() {
        for (int i = 0; i < RUNS; i++) {
            assertEquals(MissileKind.BASIC, director.nextKind(0.0, List.of(), false),
                    "all'inizio solo il basic e' sbloccato");
        }
    }

    @Test
    void clockNeverSpawnsWithoutEnoughActiveMissiles() {
        // A 60s il clock e' sbloccato, ma richiede almeno 3 missili a schermo: con lista
        // vuota non deve mai uscire.
        for (int i = 0; i < RUNS; i++) {
            assertNotEquals(MissileKind.CLOCK, director.nextKind(60.0, List.of(), false),
                    "il clock non deve uscire senza abbastanza missili attivi");
        }
    }

    @Test
    void alwaysReturnsAnUnlockedKind() {
        final List<Missile> empty = List.of();
        for (int i = 0; i < RUNS; i++) {
            final MissileKind kind = director.nextKind(15.0, empty, false);
            assertNotNull(kind);
            // A 15s sono sbloccati solo basic (0s) e bounce (12s).
            assertTrue(kind == MissileKind.BASIC || kind == MissileKind.BOUNCE,
                    "a 15s puo' uscire solo basic o bounce, non " + kind);
        }
    }
}
