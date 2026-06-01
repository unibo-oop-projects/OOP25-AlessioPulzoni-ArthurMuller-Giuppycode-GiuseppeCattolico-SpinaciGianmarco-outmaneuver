package outmaneuver.controller.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import outmaneuver.controller.MissileController;
import outmaneuver.model.area.Plane;
import outmaneuver.model.missile.BasicMissile;
import outmaneuver.model.missile.Missile;
import outmaneuver.model.missile.MissileRenderData;

public final class MissileControllerImpl implements MissileController {

    // --- COSTANTI SPAWN ---
    private static final double START_DELAY      = 3.0;
    private static final double INITIAL_INTERVAL = 2.5;
    private static final double MIN_INTERVAL     = 0.35;
    private static final double INTERVAL_SCALE   = 0.018;
    private static final int    BORDER_MARGIN    = 60;

    private final List<Missile> activeMissiles   = new ArrayList<>();
    private final List<Missile> pendingAdditions = new ArrayList<>();
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

        for (final Missile m : activeMissiles) {
            if (m.isAlive()) m.update(plane, dt);
        }

        processRemovals(plane);

        activeMissiles.addAll(pendingAdditions);
        pendingAdditions.clear();
    }

    private void spawnMissile(final Plane plane) {
        final double[] pos = randomBorderPosition(plane);
        final Missile m = new BasicMissile(pos[0], pos[1]);
        m.setInitialDirection(
                plane.getPosition().getX(),
                plane.getPosition().getY());
        activeMissiles.add(m);
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

    private void processRemovals(final Plane plane) {
        final List<Missile> toRemove = new ArrayList<>();
        for (final Missile m : activeMissiles) {
            final boolean dead      = !m.isAlive();
            final boolean offScreen = m.isOffScreen(plane, screenW, screenH);
            if (dead || offScreen) {
                if (dead) processDeathEffects(m);
                toRemove.add(m);
            }
        }
        activeMissiles.removeAll(toRemove);
    }

    private void processDeathEffects(final Missile m) {
        // Figli — SplitMissile, TwinsMissile
        pendingAdditions.addAll(m.getSpawnOnDeath());

        // Altri tipi da aggiungere dopo
        // if (m instanceof FreezeMissile) { ... }
        // if (m instanceof ClockMissile)  { ... }
    }

    @Override
    public List<MissileRenderData> getRenderData() {
        final List<MissileRenderData> result = new ArrayList<>();
        for (final Missile m : activeMissiles) {
            if (m.isAlive()) result.add(m.getRenderData());
        }
        return result;
    }

    public List<Missile> getActiveMissiles() {
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