package outmaneuver.model.missile.data;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import outmaneuver.util.json.JsonResourceLoader;

/*
 * Carica i dati dei missili da un file JSON in resources/.
 * Analogo a JsonPlaneRepository per i piani.
 */
public final class JsonMissileRepository implements MissileRepository {

    private final List<MissileData> missiles;

    public JsonMissileRepository(final JsonResourceLoader<List<MissileData>> loader) {
        Objects.requireNonNull(loader, "loader must not be null");
        this.missiles = List.copyOf(loader.load());
    }

    @Override
    public List<MissileData> loadAll() {
        return missiles;
    }

    @Override
    public Optional<MissileData> loadByType(final String type) {
        Objects.requireNonNull(type, "type must not be null");
        return missiles.stream()
                .filter(m -> m.type().equals(type))
                .findFirst();
    }
}