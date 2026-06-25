package outmaneuver.model.area.entity.collectibles;

import outmaneuver.model.area.effect.Effect;
import outmaneuver.model.area.entity.Entity;

/** A pickup entity that grants an effect to the plane when collected. */
public interface Collectible extends Entity {

    /**
     * Returns the effect granted when this collectible is picked up.
     *
     * @return the effect granted by this collectible, or {@code null} if none
     */
    Effect getEffect();

    /**
     * Returns the identifier of this collectible's kind, used e.g. for rendering.
     *
     * @return the collectible type identifier
     */
    String getCollectibleType();
}
