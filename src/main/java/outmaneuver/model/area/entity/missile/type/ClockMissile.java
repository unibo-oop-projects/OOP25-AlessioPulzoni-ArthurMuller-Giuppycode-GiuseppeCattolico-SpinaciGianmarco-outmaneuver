package outmaneuver.model.area.entity.missile.type;

import java.util.List;
import java.util.Objects;

import outmaneuver.model.area.entity.missile.Missile;
import outmaneuver.model.area.entity.missile.MissileImpl;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.model.area.entity.missile.data.MissileData.SlowEffect;
import outmaneuver.util.Vector2;

/**
 * Quando collide rallenta tutti i missili attivi (aiuta il giocatore).
 */
public final class ClockMissile extends MissileImpl {

    private final SlowEffect slow;

    /**
     * Creates a clock missile.
     *
     * @param spawnPos the initial position in world coordinates
     * @param data the missile's type definition; must include a {@code slow} effect
     */
    public ClockMissile(final Vector2 spawnPos, final MissileData data) {
        super(spawnPos, data);
        this.slow = Objects.requireNonNull(
                data.slow(), "clock missile requires a 'slow' effect in its data");
    }

    @Override
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public void onCollision(final List<Missile> activeMissiles) {
        for (final Missile other : activeMissiles) {
            if (!other.isAlive() || other == this) {
                continue;
            }
            other.slowDown(slow.factor(), slow.duration());
        }
        destroy();
    }
}
