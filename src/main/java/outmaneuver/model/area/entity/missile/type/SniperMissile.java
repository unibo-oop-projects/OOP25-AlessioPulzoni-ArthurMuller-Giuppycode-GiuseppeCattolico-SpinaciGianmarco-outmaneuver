package outmaneuver.model.area.entity.missile.type;

import java.awt.Dimension;

import outmaneuver.model.area.entity.missile.MissileImpl;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.util.Vector2;

public final class SniperMissile extends MissileImpl {

    public SniperMissile(final Vector2 spawnPos, final MissileData data) {
        super(spawnPos, data);
    }

    @Override
    protected void steer(final Vector2 target) {
    }
}
