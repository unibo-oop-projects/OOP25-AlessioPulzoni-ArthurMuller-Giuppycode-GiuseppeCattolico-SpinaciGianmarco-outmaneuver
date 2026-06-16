package outmaneuver.model.area.entity.plane;

import java.util.List;
import java.util.Optional;

public interface PlaneRepository {

    List<PlaneData> loadAll();

    Optional<PlaneData> loadById(String id);
}
