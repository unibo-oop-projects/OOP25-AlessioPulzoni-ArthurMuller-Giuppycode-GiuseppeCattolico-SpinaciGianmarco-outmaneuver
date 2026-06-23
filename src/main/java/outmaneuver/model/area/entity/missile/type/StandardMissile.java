package outmaneuver.model.area.entity.missile.type;

import outmaneuver.model.area.entity.missile.MissileImpl;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.util.Vector2;

public final class StandardMissile extends MissileImpl {

    public StandardMissile(final Vector2 spawnPos, final MissileData data) {
        super(spawnPos, data);
    }
}
