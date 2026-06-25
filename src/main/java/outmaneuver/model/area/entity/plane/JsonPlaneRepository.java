package outmaneuver.model.area.entity.plane;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import outmaneuver.util.json.JsonResourceLoader;

/** {@link PlaneRepository} that loads plane definitions from a JSON resource. */
public final class JsonPlaneRepository implements PlaneRepository {

    private final List<PlaneData> planes;

    /**
     * Creates a repository backed by the given JSON loader.
     *
     * @param loader the loader used to read the list of plane definitions
     */
    public JsonPlaneRepository(final JsonResourceLoader<List<PlaneData>> loader) {
        Objects.requireNonNull(loader, "loader must not be null");
        this.planes = List.copyOf(loader.load());
    }

    @Override
    public List<PlaneData> loadAll() {
        return planes;
    }

    @Override
    public Optional<PlaneData> loadById(final String id) {
        Objects.requireNonNull(id, "id must not be null");
        return planes.stream()
                .filter(p -> p.id().equals(id))
                .findFirst();
    }
}
