package outmaneuver.model.area.entity.missile.data;

import java.util.Optional;

/*
 * Interfaccia per accedere ai dati dei missili.
 */
public interface MissileRepository {

    Optional<MissileData> loadByType(String type);
}