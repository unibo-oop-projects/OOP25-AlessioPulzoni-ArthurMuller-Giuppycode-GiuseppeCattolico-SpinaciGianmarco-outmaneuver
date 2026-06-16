package outmaneuver.model.collectibles;

import outmaneuver.model.area.Plane;
import outmaneuver.model.session.IGameSession;

@FunctionalInterface
public interface Collectible {

    void apply(Plane plane, IGameSession session);
}
