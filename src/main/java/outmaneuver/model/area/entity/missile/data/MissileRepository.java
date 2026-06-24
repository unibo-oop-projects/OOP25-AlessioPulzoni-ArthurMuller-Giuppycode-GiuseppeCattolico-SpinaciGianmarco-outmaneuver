package outmaneuver.model.area.entity.missile.data;

import java.util.Optional;

public interface MissileRepository {

    Optional<MissileData> loadByType(String type);
}