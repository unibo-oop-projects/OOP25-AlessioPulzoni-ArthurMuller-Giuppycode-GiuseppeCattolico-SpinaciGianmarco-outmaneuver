package outmaneuver.controller.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import outmaneuver.controller.MissileController;
import outmaneuver.model.area.Plane;
import outmaneuver.model.missile.IMissile;
import outmaneuver.model.missile.type.BasicMissile;
import outmaneuver.model.missile.type.BounceMissile;
import outmaneuver.model.missile.type.ClockMissile;
import outmaneuver.model.missile.type.FastMissile;
import outmaneuver.model.missile.type.FreezeMissile;
import outmaneuver.model.missile.type.GhostMissile;
import outmaneuver.model.missile.type.ShieldMissile;
import outmaneuver.model.missile.type.SniperMissile;
import outmaneuver.model.missile.type.TwinsMissile;
import outmaneuver.view.MissileRenderData;

public final class MissileControllerImpl implements MissileController {

    // --- COSTANTI SPAWN ---
    private static final double START_DELAY      = 3.0;
    private static final double INITIAL_INTERVAL = 2.5;
    private static final double MIN_INTERVAL     = 0.35;
    private static final double INTERVAL_SCALE   = 0.018;
    private static final int    BORDER_MARGIN    = 60;

    private final List<IMissile> activeMissiles   = new ArrayList<>();
    private final List<IMissile> pendingAdditions = new ArrayList<>();
    private final int screenW;
    private final int screenH;
    private final Random rng = new Random();

    private double startDelay    = START_DELAY;
    private double spawnTimer    = 0;
    private double spawnInterval = INITIAL_INTERVAL;
    private double elapsedTime   = 0;

    public MissileControllerImpl(final int screenW, final int screenH) {
        this.screenW = screenW;
        this.screenH = screenH;
    }

    @Override
    public void update(final Plane plane, final double dt) {
        if (startDelay > 0) { startDelay -= dt; return; }

        elapsedTime += dt;
        spawnTimer  += dt;

        if (spawnTimer >= spawnInterval) {
            spawnMissile(plane);
            spawnInterval = Math.max(MIN_INTERVAL,
                    INITIAL_INTERVAL - elapsedTime * INTERVAL_SCALE);
            spawnTimer = 0;
        }

        for (final IMissile m : activeMissiles) {
            if (m.isAlive()) m.update(plane, dt);
        }

        // checkBounce per BounceMissile
        for (final IMissile m : activeMissiles) {
            if (m instanceof final BounceMissile bm) {
                bm.checkBounce(plane);
            }
        }

        // Redirect missili fuori schermo — BounceMissile escluso
        for (final IMissile m : activeMissiles) {
            if (m.isAlive() && !(m instanceof BounceMissile)) {
                m.redirectIfOutOfBounds(plane, screenW, screenH);
            }
        }

        processRemovals();

        activeMissiles.addAll(pendingAdditions);
        pendingAdditions.clear();
    }

    private void spawnMissile(final Plane plane) {
        final double[] pos = randomBorderPosition(plane);
        final IMissile m = createRandom(pos[0], pos[1], plane);
        if (!(m instanceof SniperMissile) && !(m instanceof TwinsMissile)) {
            m.setInitialDirection(
                    plane.getPosition().getX(),
                    plane.getPosition().getY());
        }
        activeMissiles.add(m);
    }

    private IMissile createRandom(final double x, final double y, final Plane plane) {
        return switch (rng.nextInt(11)) {
            case 0, 1, 2 -> new BasicMissile(x, y);
            case 3       -> new SniperMissile(x, y, plane);
            case 4       -> new BounceMissile(x, y, screenW, screenH);
            case 5       -> new GhostMissile(x, y);
            case 6       -> new FreezeMissile(x, y);
            case 7       -> new ClockMissile(x, y);
            case 8       -> new ShieldMissile(x, y);
            case 9       -> new FastMissile(x, y);
            default      -> new TwinsMissile(x, y, plane);
        };
    }

    private double[] randomBorderPosition(final Plane plane) {
        final double cx = plane.getPosition().getX();
        final double cy = plane.getPosition().getY();
        final int side  = rng.nextInt(4);
        return switch (side) {
            case 0 -> new double[]{
                cx + rng.nextDouble() * screenW - screenW / 2.0,
                cy - screenH / 2.0 - BORDER_MARGIN };
            case 1 -> new double[]{
                cx + rng.nextDouble() * screenW - screenW / 2.0,
                cy + screenH / 2.0 + BORDER_MARGIN };
            case 2 -> new double[]{
                cx - screenW / 2.0 - BORDER_MARGIN,
                cy + rng.nextDouble() * screenH - screenH / 2.0 };
            default -> new double[]{
                cx + screenW / 2.0 + BORDER_MARGIN,
                cy + rng.nextDouble() * screenH - screenH / 2.0 };
        };
    }

    private void processRemovals() {
        final List<IMissile> toRemove = new ArrayList<>();
        for (final IMissile m : activeMissiles) {
            if (!m.isAlive()) {
                processDeathEffects(m);
                toRemove.add(m);
            }
        }
        activeMissiles.removeAll(toRemove);
    }

    private void processDeathEffects(final IMissile m) {
        pendingAdditions.addAll(m.getSpawnOnDeath());
    }

    @Override
    public List<MissileRenderData> getRenderData() {
        final List<MissileRenderData> result = new ArrayList<>();
        for (final IMissile m : activeMissiles) {
            if (m.isAlive()) result.add(m.getRenderData());
        }
        return result;
    }

    public List<IMissile> getActiveMissiles() {
        return Collections.unmodifiableList(activeMissiles);
    }

    @Override
    public void reset() {
        activeMissiles.clear();
        pendingAdditions.clear();
        spawnTimer    = 0;
        elapsedTime   = 0;
        startDelay    = START_DELAY;
        spawnInterval = INITIAL_INTERVAL;
    }
}