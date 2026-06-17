package outmaneuver.controller.impl;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.IntSupplier;

import outmaneuver.controller.CollisionEngine;
import outmaneuver.controller.MissileController;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.area.collision.ICollidable;
import outmaneuver.model.area.entity.missile.Missile;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.model.area.entity.missile.data.MissileRepository;
import outmaneuver.util.Vector2;
import outmaneuver.model.area.entity.missile.type.BasicMissile;
import outmaneuver.model.area.entity.missile.type.BounceMissile;
import outmaneuver.model.area.entity.missile.type.ClockMissile;
import outmaneuver.model.area.entity.missile.type.FastMissile;
import outmaneuver.model.area.entity.missile.type.ShieldMissile;
import outmaneuver.model.area.entity.missile.type.SniperMissile;
import outmaneuver.view.MissileRenderData;

public final class MissileControllerImpl implements MissileController {

    // --- COSTANTI SPAWN ---
    private static final double START_DELAY      = 3.0;
    private static final double INITIAL_INTERVAL = 2.5;
    private static final double MIN_INTERVAL     = 0.35;
    private static final double INTERVAL_SCALE   = 0.018;
    private static final int    BORDER_MARGIN    = 60;

    // --- SOGLIE DIFFICOLTA' (secondi) ---
    private static final double TIER1_TIME = 15.0;
    private static final double TIER2_TIME = 30.0;
    private static final double TIER3_TIME = 60.0;

    private final List<Missile> activeMissiles   = new ArrayList<>();
    private final IntSupplier screenWSupplier;
    private final IntSupplier screenHSupplier;
    private final Random rng = new Random();
    private final CollisionEngine collisionEngine;
    private final MissileRepository missileRepo;

    private double startDelay    = START_DELAY;
    private double spawnTimer    = 0;
    private double spawnInterval = INITIAL_INTERVAL;
    private double elapsedTime   = 0;

    public MissileControllerImpl(final IntSupplier screenWSupplier,
                                 final IntSupplier screenHSupplier,
                                 final CollisionEngine collisionEngine,
                                 final MissileRepository missileRepo) {
        this.screenWSupplier = Objects.requireNonNull(screenWSupplier, "screenWSupplier must not be null");
        this.screenHSupplier = Objects.requireNonNull(screenHSupplier, "screenHSupplier must not be null");
        this.collisionEngine = Objects.requireNonNull(collisionEngine, "collisionEngine must not be null");
        this.missileRepo     = Objects.requireNonNull(missileRepo, "missileRepo must not be null");
    }

    @Override
    public void update(final Plane plane, final double dt) {
        if (startDelay > 0) { startDelay -= dt; return; }

        elapsedTime += dt;
        spawnTimer  += dt;

        if (spawnTimer >= spawnInterval) {
            spawnMissile(plane.getPosition());
            spawnInterval = Math.max(MIN_INTERVAL,
                    INITIAL_INTERVAL - elapsedTime * INTERVAL_SCALE);
            spawnTimer = 0;
        }

        final Dimension screen = new Dimension(screenWSupplier.getAsInt(), screenHSupplier.getAsInt());

        for (final Missile m : activeMissiles) {
            if (!m.isAlive()) continue;
            m.update(plane, dt);
            if (!m.isAlive()) continue;
            m.checkBounce(plane.getPosition(), screen);
            m.redirectIfOutOfBounds(plane, screen);
        }

        // Le collisioni vengono valutate dal game loop (MasterController) tramite collisionEngine.tick()
        processRemovals();
    }

    // Chiamato da MasterControllerImpl quando arriva MISSILE_MISSILE_COLLISION
    @Override
    public void onMissileMissileCollision(final ICollidable a, final ICollidable b) {
        handleCollisionSide(a);
        handleCollisionSide(b);
    }

    // Chiamato da MasterControllerImpl quando arriva PLANE_MISSILE_COLLISION.
    // Il game over è gestito da MasterControllerImpl: qui distruggiamo solo il missile.
    @Override
    public void onPlaneHit(final ICollidable a, final ICollidable b) {
        if (a instanceof final Missile m) {
            m.destroy();
        }
        if (b instanceof final Missile m) {
            m.destroy();
        }
    }

    private void handleCollisionSide(final ICollidable entity) {
        if (entity instanceof final Missile m) {
            m.onCollision(activeMissiles);
        }
    }

    private void spawnMissile(final Vector2 planePos) {
        final Vector2 spawnPos = randomBorderPosition(planePos);
        final Missile m = createRandom(spawnPos);
        m.setInitialDirection(planePos);
        addMissile(m);
    }

    private void addMissile(final Missile m) {
        activeMissiles.add(m);
        collisionEngine.register(m);
    }

    private Missile createRandom(final Vector2 spawnPos) {
        final String type = randomType();
        final MissileData data = missileRepo.loadByType(type).orElseThrow(
                () -> new IllegalStateException("Missile type not found: " + type));
        return switch (type) {
            case "basic"  -> new BasicMissile(spawnPos, data);
            case "fast"   -> new FastMissile(spawnPos, data);
            case "sniper" -> new SniperMissile(spawnPos, data);
            case "bounce" -> new BounceMissile(spawnPos, data);
            case "clock"  -> new ClockMissile(spawnPos, data);
            case "shield" -> new ShieldMissile(spawnPos, data);
            default -> new BasicMissile(spawnPos, data);
        };
    }

    private String randomType() {
        if (elapsedTime < TIER1_TIME) {
            // Solo basic
            return "basic";
        } else if (elapsedTime < TIER2_TIME) {
            // basic, fast, sniper
            return switch (rng.nextInt(5)) {
                case 0, 1, 2 -> "basic";
                case 3       -> "fast";
                default      -> "sniper";
            };
        } else if (elapsedTime < TIER3_TIME) {
            // Aggiunge bounce
            return switch (rng.nextInt(6)) {
                case 0, 1, 2 -> "basic";
                case 3       -> "fast";
                case 4       -> "sniper";
                default      -> "bounce";
            };
        } else {
            // Tutti i tipi
            return switch (rng.nextInt(8)) {
                case 0, 1, 2 -> "basic";
                case 3       -> "sniper";
                case 4       -> "bounce";
                case 5       -> "clock";
                case 6       -> "shield";
                default      -> "fast";
            };
        }
    }

    private Vector2 randomBorderPosition(final Vector2 planePos) {
        final Dimension screen = new Dimension(screenWSupplier.getAsInt(), screenHSupplier.getAsInt());
        final double cx = planePos.getX();
        final double cy = planePos.getY();
        final int side = rng.nextInt(4);
        return switch (side) {
            case 0 -> new Vector2(cx + rng.nextDouble() * screen.width  - screen.width  / 2.0, cy - screen.height / 2.0 - BORDER_MARGIN);
            case 1 -> new Vector2(cx + rng.nextDouble() * screen.width  - screen.width  / 2.0, cy + screen.height / 2.0 + BORDER_MARGIN);
            case 2 -> new Vector2(cx - screen.width  / 2.0 - BORDER_MARGIN, cy + rng.nextDouble() * screen.height - screen.height / 2.0);
            default -> new Vector2(cx + screen.width  / 2.0 + BORDER_MARGIN, cy + rng.nextDouble() * screen.height - screen.height / 2.0);
        };
    }

    private void processRemovals() {
        activeMissiles.removeIf(m -> {
            if (m.isAlive()) {
                return false;
            }
            collisionEngine.unregister(m);
            return true;
        });
    }

    @Override
    public List<MissileRenderData> getRenderData() {
        final List<MissileRenderData> result = new ArrayList<>();
        for (final Missile m : activeMissiles) {
            if (m.isAlive()) result.add(m.getRenderData());
        }
        return result;
    }

    @Override
    public List<Missile> getActiveMissiles() {
        return Collections.unmodifiableList(activeMissiles);
    }

    @Override
    public void reset() {
        for (final Missile m : activeMissiles) {
            collisionEngine.unregister(m);
        }
        activeMissiles.clear();

        spawnTimer    = 0;
        elapsedTime   = 0;
        startDelay    = START_DELAY;
        spawnInterval = INITIAL_INTERVAL;
    }
}
