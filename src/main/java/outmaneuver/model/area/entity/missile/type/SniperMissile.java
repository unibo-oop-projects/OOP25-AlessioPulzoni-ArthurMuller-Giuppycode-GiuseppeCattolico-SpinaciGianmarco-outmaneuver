package outmaneuver.model.area.entity.missile.type;

import outmaneuver.model.area.entity.missile.MissileImpl;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.util.Vector2;

/** A missile that travels in a straight line without steering towards the plane. */
public final class SniperMissile extends MissileImpl {

    /**
     * Creates a sniper missile.
     *
     * @param spawnPos the initial position in world coordinates
     * @param data the missile's type definition
     */
    public SniperMissile(final Vector2 spawnPos, final MissileData data) {
        super(spawnPos, data);
    }

    @Override
    protected void steer(final Vector2 target) {
    }
}
