package outmaneuver.model.area.entity.missile.data;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import outmaneuver.util.json.JsonResourceLoader;

/** {@link MissileRepository} that loads missile definitions from a JSON resource. */
public final class JsonMissileRepository implements MissileRepository {

    private final List<MissileData> missiles;

    /**
     * Creates a repository backed by the given JSON loader.
     *
     * @param loader the loader used to read the list of missile definitions
     */
    public JsonMissileRepository(final JsonResourceLoader<List<MissileData>> loader) {
        Objects.requireNonNull(loader, "loader must not be null");
        this.missiles = List.copyOf(loader.load());
    }

    @Override
    public Optional<MissileData> loadByType(final String type) {
        Objects.requireNonNull(type, "type must not be null");
        return missiles.stream()
                .filter(m -> m.type().equals(type))
                .findFirst();
    }
}
