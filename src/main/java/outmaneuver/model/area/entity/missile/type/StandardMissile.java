package outmaneuver.model.area.entity.missile.type;

import outmaneuver.model.area.entity.missile.MissileImpl;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.util.Vector2;

/** The default missile type: steers towards the plane with no special behaviour. */
public final class StandardMissile extends MissileImpl {

    /**
     * Creates a standard missile.
     *
     * @param spawnPos the initial position in world coordinates
     * @param data the missile's type definition
     */
    public StandardMissile(final Vector2 spawnPos, final MissileData data) {
        super(spawnPos, data);
    }
}
