package outmaneuver.controller;

import java.util.function.BiFunction;

import outmaneuver.model.area.entity.missile.Missile;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.model.area.entity.missile.type.BounceMissile;
import outmaneuver.model.area.entity.missile.type.ClockMissile;
import outmaneuver.model.area.entity.missile.type.ShieldMissile;
import outmaneuver.model.area.entity.missile.type.SniperMissile;
import outmaneuver.model.area.entity.missile.type.StandardMissile;
import outmaneuver.util.Vector2;

/**
 * Catalogo dei tipi di missile con i loro metadati di difficolta'.
 * Per ogni tipo, in un unico posto:
 *  - id          : la stringa usata nel JSON e nel render;
 *  - weight      : peso di COMPLESSITA' (quanto e' tosto / quanta pressione mette);
 *  - unlockTime  : da quanti secondi di gioco puo' comparire;
 *  - cap         : quanti al massimo possono stare a schermo insieme;
 *  - minActive   : quanti missili devono gia' essere a schermo perche' possa comparire
 *                  (es. il clock ha senso solo se c'e' qualcosa da rallentare);
 *  - factory     : come si costruisce.
 *
 * I pesi (da ragionamento: schivata x persistenza x se aiuta il giocatore):
 *   clock 0.8 (facile e AIUTA) < basic 1.0 < bounce 1.5 (non muore mai) <
 *   sniper 2.0 (veloce ma passa) < shield 2.5 (doppia vita) < fast 3.0 (il piu' duro).
 *
 * Aggiungere un nuovo tipo di missile = aggiungere una sola riga qui.
 */
public enum MissileKind {

    // id        peso  sblocco(s)  cap  minAttivi  factory
    BASIC ("basic",  1.0,   0.0,  99,  0, StandardMissile::new),
    BOUNCE("bounce", 1.5,  12.0,   3,  0, BounceMissile::new),
    SNIPER("sniper", 2.0,  28.0,   4,  0, SniperMissile::new),
    FAST  ("fast",   3.0,  45.0,   2,  0, StandardMissile::new),
    CLOCK ("clock",  0.8,  55.0,   2,  3, ClockMissile::new),
    SHIELD("shield", 2.5,  55.0,   2,  0, ShieldMissile::new);

    private final String id;
    private final double weight;
    private final double unlockTime;
    private final int cap;
    private final int minActive;
    private final BiFunction<Vector2, MissileData, Missile> factory;

    MissileKind(final String id, final double weight, final double unlockTime,
                final int cap, final int minActive,
                final BiFunction<Vector2, MissileData, Missile> factory) {
        this.id = id;
        this.weight = weight;
        this.unlockTime = unlockTime;
        this.cap = cap;
        this.minActive = minActive;
        this.factory = factory;
    }

    public String id() {
        return id;
    }

    double weight() {
        return weight;
    }

    int cap() {
        return cap;
    }

    int minActive() {
        return minActive;
    }

    boolean isUnlockedAt(final double elapsedTime) {
        return elapsedTime >= unlockTime;
    }

    public Missile create(final Vector2 spawnPos, final MissileData data) {
        return factory.apply(spawnPos, data);
    }

    static MissileKind fromId(final String id) {
        for (final MissileKind k : values()) {
            if (k.id.equals(id)) {
                return k;
            }
        }
        throw new IllegalArgumentException("Unknown missile kind: " + id);
    }
}
