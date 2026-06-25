package outmaneuver.model.area.entity.missile.data;

import java.util.Optional;

/** Provides lookup of {@link MissileData} definitions by missile type. */
@FunctionalInterface
public interface MissileRepository {

    /**
     * Looks up the missile definition for the given type.
     *
     * @param type the missile type identifier
     * @return the matching missile definition, or empty if none is found
     */
    Optional<MissileData> loadByType(String type);
}
