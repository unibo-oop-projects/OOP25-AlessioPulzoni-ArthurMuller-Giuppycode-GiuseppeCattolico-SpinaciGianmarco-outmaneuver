package outmaneuver.model.area.entity.collectibles;

import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.Entity;
import outmaneuver.model.session.IGameSession;


public interface Collectible extends Entity {

    void apply(Plane plane, IGameSession session);
}
