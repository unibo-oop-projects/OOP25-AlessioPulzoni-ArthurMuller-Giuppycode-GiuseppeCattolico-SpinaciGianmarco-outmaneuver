package outmaneuver.model.area.entity.missile.type;

import outmaneuver.model.area.entity.missile.MissileImpl;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.util.Vector2;

/**
 * Missile "standard": insegue il giocatore col comportamento di default.
 * Usato dai tipi senza logica speciale (basic, fast), che differiscono solo per i
 * parametri (velocita', rotazione...) presi dal JSON.
 */
public final class StandardMissile extends MissileImpl {

    public StandardMissile(final Vector2 spawnPos, final MissileData data) {
        super(spawnPos, data);
    }
}
