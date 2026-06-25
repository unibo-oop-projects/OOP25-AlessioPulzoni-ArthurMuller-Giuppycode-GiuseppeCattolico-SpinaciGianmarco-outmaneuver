package outmaneuver.model.area.entity.missile.type;

import java.util.List;

import outmaneuver.model.area.entity.missile.Missile;
import outmaneuver.model.area.entity.missile.MissileImpl;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.util.Vector2;

/**
 * Richiede due collisioni per essere distrutto (regge il primo colpo).
 */
public final class ShieldMissile extends MissileImpl {

    private boolean shielded = true;

    /**
     * Creates a shield missile.
     *
     * @param spawnPos the initial position in world coordinates
     * @param data the missile's type definition
     */
    public ShieldMissile(final Vector2 spawnPos, final MissileData data) {
        super(spawnPos, data);
    }

    @Override
    public void onCollision(final List<Missile> activeMissiles) {
        if (shielded) {
            shielded = false;
        } else {
            destroy();
        }
    }
}
