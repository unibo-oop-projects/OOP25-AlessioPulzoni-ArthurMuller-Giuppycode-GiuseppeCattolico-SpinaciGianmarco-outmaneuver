package outmaneuver.controller.impl.missile;

import java.awt.Dimension;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import outmaneuver.controller.CollisionEngine;
import outmaneuver.controller.impl.EntityControllerImpl;
import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.missile.Missile;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.model.area.entity.missile.data.MissileRepository;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.util.Vector2;

/**
 * Controller dei missili: decide quando e dove farli nascere, li muove e
 * reagisce alle loro collisioni. Il rilevamento delle collisioni spetta al
 * CollisionEngine; la reazione vera (shield/clock/destroy) ai metodi del
 * missile.
 */
public final class MissileControllerImpl extends EntityControllerImpl {

    // --- COSTANTI SPAWN (la "leva quantita'") ---
    // L'intervallo tra due spawn parte da INITIAL_INTERVAL e cala nel tempo
    // (INITIAL_INTERVAL - elapsedTime * INTERVAL_SCALE) fino a MIN_INTERVAL.
    // Tarato (via simulazione) per una partita di ~5 minuti a chi gioca bene:
    // inizio morbido (6.5s), ritmo massimo (0.5s) verso i 5 minuti -> lo schermo
    // si riempie e si diventa ingiocabili.
    private static final double START_DELAY = 4.0;
    private static final double INITIAL_INTERVAL = 6.5;
    private static final double MIN_INTERVAL = 0.5;
    private static final double INTERVAL_SCALE = 0.020;
    private static final int BORDER_MARGIN = 60;
    // Quota di spawn dal LATO CORTO dello schermo. Di solito il player si muove
    // lungo il
    // lato lungo (piu' pista); i missili dal lato corto gli arrivano lungo
    // quell'asse, quindi
    // ha poco campo per schivare ed e' costretto a cambiare direzione.
    private static final double SHORT_SIDE_BIAS = 0.6;

    private final MissileRepository missileRepo;
    private final MissileSpawnDirector spawnDirector;
    private final Random rng = new Random();

    private double startDelay = START_DELAY;
    // timer "gia' pieno": il primo missile esce subito dopo START_DELAY (~4s), non
    // dopo un intervallo intero
    private double spawnTimer = INITIAL_INTERVAL;
    private double spawnInterval = INITIAL_INTERVAL;
    private double elapsedTime;
    private boolean shieldActive;
    private double speedMultiplier = 1.0;

    /**
     * Creates a controller that spawns and moves missiles within the given shared
     * entity list.
     *
     * @param entities the shared list of entities in the scene
     * @param collisionEngine the collision engine missiles register with
     * @param missileRepo the repository used to load missile data by type
     * @param spawnDirector the director deciding which missile kind to spawn next
     */
    public MissileControllerImpl(final List<Entity> entities,
            final CollisionEngine collisionEngine,
            final MissileRepository missileRepo,
            final MissileSpawnDirector spawnDirector) {
        super(entities, collisionEngine);
        this.missileRepo = Objects.requireNonNull(missileRepo, "missileRepo must not be null");
        this.spawnDirector = Objects.requireNonNull(spawnDirector, "spawnDirector must not be null");
    }

    @Override
    public void updateEntities(final long deltaMs) {
        final double dt = deltaMs / 1000.0;
        if (startDelay > 0) {
            startDelay -= dt;
            return;
        }
        final Optional<Plane> planeOpt = findPlane();
        if (planeOpt.isEmpty() || getView() == null) {
            return;
        }
        final Plane plane = planeOpt.get();
        final Dimension screen = new Dimension(getView().getWidth(), getView().getHeight());

        elapsedTime += dt;
        maybeSpawn(dt, plane, screen);
        moveMissiles(plane, screen, dt);
    }

    @Override
    public void clearAll() {
        startDelay = START_DELAY;
        spawnTimer = INITIAL_INTERVAL;
        elapsedTime = 0;
        spawnInterval = INITIAL_INTERVAL;
        // azzero anche gli effetti: senza questo, dopo un "Play Again" restavano
        // lo scudo o lo speed boost dell'ultima partita (stato sporco)
        shieldActive = false;
        speedMultiplier = 1.0;
    }

    // AGGIUNTO: reso public per far reagire i missili nell'EventController (lista passata a onCollision)
    /**
     * Returns the missiles currently managed by this controller.
     *
     * @return the list of active missile entities
     */
    public List<Missile> activeMissiles() {
        return getEntities().stream()
                .filter(Missile.class::isInstance)
                .map(Missile.class::cast)
                .toList();
    }

    /**
     * Sets whether the player's shield is active, which missiles may take into account
     * when reacting to collisions.
     *
     * @param active {@code true} if the shield is active
     */
    public void setShieldActive(final boolean active) {
        this.shieldActive = active;
    }

    /**
     * Sets the multiplier applied to the plane's speed, used to keep missile movement
     * consistent with the plane's effective speed.
     *
     * @param multiplier the speed multiplier to apply
     */
    public void setSpeedMultiplier(final double multiplier) {
        this.speedMultiplier = multiplier;
    }

    private void maybeSpawn(final double dt, final Plane plane, final Dimension screen) {
        spawnTimer += dt;
        if (spawnTimer < spawnInterval) {
            return;
        }
        spawnTimer = 0;
        spawnInterval = Math.max(MIN_INTERVAL, INITIAL_INTERVAL - elapsedTime * INTERVAL_SCALE);

        final Vector2 planePos = plane.getPosition();
        final MissileKind kind = spawnDirector.nextKind(elapsedTime, activeMissiles(), shieldActive);
        // Il fast e' molto veloce: dal lato lungo, di fronte all'aereo, e' quasi
        // inevitabile.
        // Lo costringo a nascere SEMPRE dal lato corto (piu' equo da schivare).
        final double shortBias = (kind == MissileKind.FAST) ? 1.0 : SHORT_SIDE_BIAS;
        final Missile m = createMissile(kind, randomBorderPosition(planePos, screen, shortBias));
        m.setInitialDirection(planePos);
        spawnEntity(m);
    }

    private void moveMissiles(final Plane plane, final Dimension screen, final double dt) {
        final double effectiveSpeed = plane.getStats().getBaseSpeed() * speedMultiplier;
        for (final Missile m : activeMissiles()) {
            if (m.isAlive()) {
                m.update(plane, dt);
            }
            if (m.isAlive()) {
                m.checkBounce(plane.getPosition(), screen);
                m.redirectIfOutOfBounds(plane, screen, effectiveSpeed);
            }
            if (!m.isAlive()) {
                removeEntity(m);
            }
        }
    }

    private Optional<Plane> findPlane() {
        return getEntities().stream()
                .filter(e -> e instanceof Plane)
                .map(e -> (Plane) e)
                .findFirst();
    }

    private Missile createMissile(final MissileKind kind, final Vector2 spawnPos) {
        // Il tipo l'ha gia' deciso il director; qui si carica il dato e si istanzia.
        final MissileData data = missileRepo.loadByType(kind.id()).orElseThrow(
                () -> new IllegalStateException("Missile type not found: " + kind.id()));
        return kind.create(spawnPos, data);
    }

    private Vector2 randomBorderPosition(final Vector2 planePos, final Dimension screen,
            final double shortSideProb) {
        final double halfW = screen.width / 2.0;
        final double halfH = screen.height / 2.0;

        // Scelgo se nascere dal lato corto o lungo (probabilita' passata dal
        // chiamante).
        final boolean fromShortSide = rng.nextDouble() < shortSideProb;
        // In landscape il lato corto sono i bordi verticali (sx/dx); in portrait quelli
        // orizzontali (su/giu').
        final boolean landscape = screen.width >= screen.height;
        final boolean horizontalEdge = landscape != fromShortSide;

        // Punto di spawn = posizione dell'aereo (centro schermo) + scostamento verso il
        // bordo.
        if (horizontalEdge) {
            // bordo sopra o sotto: x casuale lungo la larghezza, y appena fuori
            final double offX = (rng.nextDouble() * 2 - 1) * halfW;
            final double offY = rng.nextBoolean() ? -halfH - BORDER_MARGIN : halfH + BORDER_MARGIN;
            return planePos.add(new Vector2(offX, offY));
        }
        // bordo sinistro o destro: y casuale lungo l'altezza, x appena fuori
        final double offY = (rng.nextDouble() * 2 - 1) * halfH;
        final double offX = rng.nextBoolean() ? -halfW - BORDER_MARGIN : halfW + BORDER_MARGIN;
        return planePos.add(new Vector2(offX, offY));
    }
}
