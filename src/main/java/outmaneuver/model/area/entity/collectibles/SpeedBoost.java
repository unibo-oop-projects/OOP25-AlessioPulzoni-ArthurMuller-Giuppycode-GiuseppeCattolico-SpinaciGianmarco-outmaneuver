package outmaneuver.model.area.entity.collectibles;

import outmaneuver.model.area.effect.Effect;
import outmaneuver.util.Vector2;

/** A collectible that grants the plane a speed boost effect when picked up. */
public final class SpeedBoost extends AbstractCollectible {

    /**
     * Creates a speed boost power-up.
     *
     * @param position the spawn position in world coordinates
     * @param effect the speed boost effect granted on pickup
     */
    public SpeedBoost(final Vector2 position, final Effect effect) {
        super(position, effect);
    }

    @Override
    public String getCollectibleType() {
        return "speed";
    }
}
