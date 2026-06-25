package outmaneuver.model.area.entity.plane;

import java.util.List;
import java.util.Optional;

/** Provides access to the catalog of available plane definitions. */
public interface PlaneRepository {

    /**
     * Returns all available plane definitions.
     *
     * @return the list of all plane definitions
     */
    List<PlaneData> loadAll();

    /**
     * Looks up the plane definition with the given identifier.
     *
     * @param id the plane type identifier
     * @return the matching plane definition, or empty if none is found
     */
    Optional<PlaneData> loadById(String id);
}
