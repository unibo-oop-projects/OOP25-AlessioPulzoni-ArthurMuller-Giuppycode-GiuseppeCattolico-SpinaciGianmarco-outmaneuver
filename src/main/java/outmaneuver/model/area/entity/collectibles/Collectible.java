package outmaneuver.model.area.entity.collectibles;

import outmaneuver.model.area.effect.Effect;
import outmaneuver.model.area.entity.Entity;

public interface Collectible extends Entity {

    Effect getEffect();
}
