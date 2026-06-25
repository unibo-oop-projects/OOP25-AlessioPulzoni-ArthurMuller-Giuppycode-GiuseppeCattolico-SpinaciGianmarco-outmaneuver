package outmaneuver.controller.impl.missile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import outmaneuver.model.area.entity.missile.Missile;

/**
 * Decide QUALE tipo di missile spawnare. Si occupa della "qualita'" (quanto tosto
 * e' in media ogni missile); la "quantita'" (quanti) la guida l'intervallo di spawn
 * del controller dei missili. Insieme danno la difficolta' totale.
 *
 * <p>Il modello e' la composizione di tre funzioni (tarate via simulazione):
 *
 * <pre>
 *   1) STIMA  - difficolta' media-per-missile desiderata al tempo t (sigmoide):
 *          D(t) = D_MIN + (D_MAX - D_MIN) / (1 + e^(-k (t - t0)))
 *
 *   2) DIVARIO - quanto siamo lontani dalla stima, normalizzato in [-1, 1] (tanh):
 *          p = tanh( ALPHA * (Savg - D(t)) )
 *      Savg = difficolta' media attuale dei missili vivi (- bonus se il player ha lo scudo).
 *      p > 0  -> piu' difficile del previsto -> spingi verso i FACILI;
 *      p < 0  -> piu' facile del previsto    -> spingi verso i TOSTI.
 *
 *   3) SCELTA - probabilita' di ogni tipo (softmax / Boltzmann):
 *          w_i = e^(-BETA * p * c_i)        c_i = peso del tipo / peso massimo
 *          P(i) = w_i / somma(w_j)
 * </pre>
 *
 * <p>Con due regole sopra: ogni tipo si sblocca a un certo tempo e ha un tetto a schermo.
 * Classe pura (stato solo nel generatore casuale): testabile in isolamento con un seed.
 */
public final class MissileSpawnDirector {

    // (1) STIMA: sigmoide della difficolta' media-per-missile desiderata.
    private static final double D_MIN = 1.0; // inizio: missili in media facili
    private static final double D_MAX = 1.95; // tardi: media piu' tosta
    private static final double D_MID = 140.0; // flesso (s): meta' della crescita
    private static final double D_STEEP = 0.022; // ripidita' della S

    // (2) DIVARIO: ripidita' del tanh e sconto se il player ha lo scudo.
    private static final double ALPHA = 1.0;
    private static final double SHIELD_RELIEF = 0.7; // scudo attivo -> Savg abbassato

    // (3) SCELTA: nettezza del softmax e bonus al clock quando lo schermo e' pieno.
    private static final double BETA = 2.2;
    private static final double CLOCK_RELIEF = 0.3; // il clock aiuta: piu' probabile se p>0

    // Peso massimo tra i tipi: serve a normalizzare le complessita' c_i in [0, 1].
    private static final double MAX_WEIGHT = maxWeight();

    private final Random rng;

    /** Creates a director using a non-seeded random generator. */
    public MissileSpawnDirector() {
        this(new Random());
    }

    /**
     * Creates a director using the given random generator, allowing deterministic
     * behavior in tests via a seeded instance.
     *
     * @param rng the random generator used for weighted kind selection
     */
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Random is intentionally shared so callers can inject a seeded instance for testing")
    public MissileSpawnDirector(final Random rng) {
        this.rng = rng;
    }

    /**
     * Sceglie il tipo del prossimo missile.
     *
     * @param elapsedTime  secondi di gioco trascorsi (t)
     * @param active       missili attualmente in scena
     * @param shieldActive true se il giocatore ha lo scudo attivo (abbassa la difficolta')
     * @return il tipo scelto
     */
    public MissileKind nextKind(final double elapsedTime, final List<Missile> active,
                                final boolean shieldActive) {
        // (2) divario p in [-1, 1]
        double savg = averageThreat(active);
        if (shieldActive) {
            savg = Math.max(0.0, savg - SHIELD_RELIEF);
        }
        final double p = Math.tanh(ALPHA * (savg - targetAverage(elapsedTime)));

        // (3) pesi softmax sui tipi disponibili: sbloccati, non al tetto, e con abbastanza
        //     missili gia' a schermo (es. il clock serve solo se c'e' qualcosa da rallentare).
        final long alive = active.stream().filter(Missile::isAlive).count();
        final List<Weighted> pool = new ArrayList<>();
        for (final MissileKind kind : MissileKind.values()) {
            if (!kind.isUnlockedAt(elapsedTime)
                    || countOf(kind, active) >= kind.cap()
                    || alive < kind.minActive()) {
                continue;
            }
            pool.add(new Weighted(kind, weightFor(kind, p)));
        }
        final MissileKind chosen = pickWeighted(pool);
        return chosen != null ? chosen : MissileKind.BASIC;
    }

    /**
     * (1) STIMA: D(t) = D_MIN + (D_MAX - D_MIN) / (1 + e^(-k (t - t0))).
     *
     * @param elapsedTime secondi di gioco trascorsi (t)
     * @return la difficolta' media-per-missile desiderata in quel momento
     */
    private double targetAverage(final double elapsedTime) {
        return D_MIN + (D_MAX - D_MIN) / (1.0 + Math.exp(-D_STEEP * (elapsedTime - D_MID)));
    }

    /**
     * (3) peso del tipo: w = e^(-BETA p c) [+ bonus clock quando lo schermo e' pieno].
     *
     * @param kind il tipo di missile di cui calcolare il peso
     * @param p il divario normalizzato in [-1, 1] tra difficolta' attuale e desiderata
     * @return il peso (non normalizzato) usato nella selezione softmax
     */
    private double weightFor(final MissileKind kind, final double p) {
        final double c = kind.weight() / MAX_WEIGHT;
        double w = Math.exp(-BETA * p * c);
        if (kind == MissileKind.CLOCK) {
            w += CLOCK_RELIEF * Math.max(0.0, p);
        }
        return w;
    }

    /**
     * Difficolta' media dei missili vivi: Savg = (somma pesi) / (numero), 0 se vuoto.
     *
     * @param active missili attualmente in scena
     * @return la difficolta' media dei missili vivi, o 0 se nessuno e' vivo
     */
    private double averageThreat(final List<Missile> active) {
        double sum = 0.0;
        int n = 0;
        for (final Missile m : active) {
            if (m.isAlive()) {
                sum += MissileKind.fromId(m.getMissileType()).weight();
                n++;
            }
        }
        return n > 0 ? sum / n : 0.0;
    }

    private long countOf(final MissileKind kind, final List<Missile> active) {
        return active.stream()
                .filter(Missile::isAlive)
                .filter(m -> kind.id().equals(m.getMissileType()))
                .count();
    }

    /**
     * Estrazione casuale pesata sui pesi del softmax.
     *
     * @param pool i tipi candidati con il rispettivo peso
     * @return il tipo estratto, o {@code null} se il pool e' vuoto o a peso totale nullo
     */
    private MissileKind pickWeighted(final List<Weighted> pool) {
        final double total = pool.stream().mapToDouble(Weighted::weight).sum();
        if (total <= 0) {
            return null;
        }
        double roll = rng.nextDouble() * total;
        for (final Weighted w : pool) {
            roll -= w.weight();
            if (roll < 0) {
                return w.kind();
            }
        }
        return null;
    }

    private static double maxWeight() {
        double max = 0.0;
        for (final MissileKind k : MissileKind.values()) {
            max = Math.max(max, k.weight());
        }
        return max;
    }

    private record Weighted(MissileKind kind, double weight) { }
}
