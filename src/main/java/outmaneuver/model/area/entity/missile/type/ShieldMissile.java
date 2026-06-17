package outmaneuver.model.area.entity.missile.type;

import java.util.List;

import outmaneuver.model.area.entity.missile.Missile;
import outmaneuver.model.area.entity.missile.MissileImpl;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.util.Vector2;

/*
 * Richiede due collisioni per essere distrutto.
 */
public final class ShieldMissile extends MissileImpl {

    private boolean shielded = true;

    public ShieldMissile(final Vector2 spawnPos, final MissileData data) {
        super(spawnPos, data.speed(), data.maxTurn(), data.radius(), data.lifetime(), data.predictionTime(), (int) data.outOfBoundsMargin());
    }

    @Override
    public void onCollision(final List<Missile> activeMissiles) {
        if (shielded) {
            shielded = false;
        } else {
            destroy();
        }
    }

    @Override
    public String getMissileType() { return "shield"; }
}