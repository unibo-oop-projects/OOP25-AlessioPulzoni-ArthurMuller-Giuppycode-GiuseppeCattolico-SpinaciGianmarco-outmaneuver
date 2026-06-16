package outmaneuver.model.missile.data;

import java.util.List;
import java.util.Optional;

/*
 * Interfaccia per accedere ai dati dei missili.
 */
public interface MissileRepository {

    List<MissileData> loadAll();

    Optional<MissileData> loadByType(String type);
}