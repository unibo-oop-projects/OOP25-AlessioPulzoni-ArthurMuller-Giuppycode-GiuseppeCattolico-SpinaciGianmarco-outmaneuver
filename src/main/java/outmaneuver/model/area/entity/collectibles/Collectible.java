package outmaneuver.model.area.entity.collectibles;

import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.session.IGameSession;

@FunctionalInterface
public interface Collectible {

    void apply(Plane plane, IGameSession session);
}
