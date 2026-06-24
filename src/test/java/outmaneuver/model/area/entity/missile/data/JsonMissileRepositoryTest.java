package outmaneuver.model.area.entity.missile.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.util.json.GsonProvider;
import outmaneuver.util.json.JsonResourceLoader;

/**
 * Verifica che missiles.json venga caricato correttamente, in particolare che
 * l'effetto opzionale {@link MissileData.SlowEffect} sia presente solo per il
 * clock e assente (null) per tutti gli altri tipi.
 */
class JsonMissileRepositoryTest {

    private MissileRepository repo;

    @BeforeEach
    void setUp() {
        repo = new JsonMissileRepository(
                JsonResourceLoader.forList("missiles.json", MissileData.class, GsonProvider.create()));
    }

    @Test
    void allKnownTypesLoad() {
        for (final String type : List.of("basic", "fast", "sniper", "bounce", "shield", "clock")) {
            assertTrue(repo.loadByType(type).isPresent(), "manca il tipo: " + type);
        }
    }

    @Test
    void unknownTypeIsEmpty() {
        assertTrue(repo.loadByType("does-not-exist").isEmpty());
    }

    @Test
    void basicHasNoSlowEffect() {
        final MissileData basic = repo.loadByType("basic").orElseThrow();
        assertNull(basic.slow(), "solo il clock deve avere l'effetto slow");
    }

    @Test
    void clockHasSlowEffect() {
        final MissileData clock = repo.loadByType("clock").orElseThrow();
        final MissileData.SlowEffect slow = clock.slow();
        assertNotNull(slow, "il clock deve avere l'effetto slow");
        assertEquals(0.3, slow.factor(), 1e-9);
        assertEquals(3.0, slow.duration(), 1e-9);
    }

    @Test
    void commonFieldsAreParsed() {
        final Optional<MissileData> basic = repo.loadByType("basic");
        assertTrue(basic.isPresent());
        final MissileData data = basic.get();
        assertEquals(320.0, data.speed(), 1e-9);
        assertEquals(15.0, data.radius(), 1e-9);
        assertEquals(15.0, data.lifetime(), 1e-9);
        assertEquals(150, data.outOfBoundsMargin());
    }
}
