package outmaneuver.model.area.entity.collectibles;

import outmaneuver.model.area.effect.Effect;
import outmaneuver.util.Vector2;

/** A collectible that grants the plane a shield effect when picked up. */
public final class ShieldPowerUp extends AbstractCollectible {

    /**
     * Creates a shield power-up.
     *
     * @param position the spawn position in world coordinates
     * @param effect the shield effect granted on pickup
     */
    public ShieldPowerUp(final Vector2 position, final Effect effect) {
        super(position, effect); // Initialize position with the provided value and effect
    }

    @Override
    public String getCollectibleType() {
        return "shield";
    }
}
