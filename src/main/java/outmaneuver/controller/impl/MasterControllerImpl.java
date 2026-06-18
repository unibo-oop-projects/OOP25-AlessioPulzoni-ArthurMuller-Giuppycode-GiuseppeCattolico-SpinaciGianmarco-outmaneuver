package outmaneuver.controller.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import outmaneuver.controller.CollisionEngine;
import outmaneuver.controller.EntityController;
import outmaneuver.controller.HudController;
import outmaneuver.controller.InternalEvent;
import outmaneuver.controller.MasterController;
import outmaneuver.controller.OutmaneuverEvent;
import outmaneuver.controller.ScoreController;

import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.area.collision.CollisionData;
import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.collectibles.Collectible;
import outmaneuver.view.EntityRenderData;
import outmaneuver.view.GameView;
import outmaneuver.view.RenderState;

public final class MasterControllerImpl implements MasterController {

    private static final long TICK_PERIOD_MS = 16;
    private static final long MAX_DELTA_MS = 50;

    private final List<GameView> views = new ArrayList<>();
    private final List<EntityController> entityControllers = new ArrayList<>();
    private final HudController hudController;
    private ScoreController scoreController;
    private EntityController primaryEntityController;
    private CollisionEngine collisionEngine;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> tickTask;
    private volatile boolean paused;
    private long lastTickTime;
    private Runnable onGameOver;
    private Runnable onPause;
    private Runnable onResume;

    public MasterControllerImpl(final HudController hudController) {
        this.hudController = Objects.requireNonNull(hudController, "hudController must not be null");
        this.paused = false;
    }

    public void setOnGameOver(final Runnable onGameOver) {
        this.onGameOver = Objects.requireNonNull(onGameOver);
    }

    public void setOnPause(final Runnable onPause) {
        this.onPause = Objects.requireNonNull(onPause);
    }

    public void setOnResume(final Runnable onResume) {
        this.onResume = Objects.requireNonNull(onResume);
    }

    public void addEntityController(final EntityController entityController) {
        Objects.requireNonNull(entityController, "entityController must not be null");
        if (primaryEntityController == null) {
            primaryEntityController = entityController;
        }
        entityControllers.add(entityController);
    }

    public void setCollisionEngine(final CollisionEngine collisionEngine) {
        if (this.collisionEngine != null) {
            throw new IllegalStateException("collisionEngine already set");
        }
        this.collisionEngine = Objects.requireNonNull(collisionEngine, "collisionEngine must not be null");
    }

    public void setScoreController(final ScoreController scoreController) {
        this.scoreController = Objects.requireNonNull(scoreController, "scoreController must not be null");
    }

    @Override
    public void handleEvent(final OutmaneuverEvent event) {
        switch (event) {
            case TOGGLE_PAUSE -> {
                if (paused) {
                    paused = false;
                    lastTickTime = System.nanoTime();
                    if (onResume != null) {
                        onResume.run();
                    }
                } else {
                    paused = true;
                    if (onPause != null) {
                        onPause.run();
                    }
                }
            }
            case QUIT_APPLICATION -> {
                shutdown();
                System.exit(0);
            }
            case GAME_OVER -> {
                stop();
                if (onGameOver != null) {
                    onGameOver.run();
                }
            }
        }
    }

    @Override
    public void attachView(final GameView view) {
        views.add(Objects.requireNonNull(view, "view must not be null"));
        entityControllers.forEach(ec -> ec.setView(view));
    }

    @Override
    public void start() {
        if (entityControllers.isEmpty()) {
            throw new IllegalStateException("at least one entityController must be added before start()");
        }
        Objects.requireNonNull(collisionEngine, "collisionEngine must be set before start()");
        if (tickTask != null && !tickTask.isCancelled()) {
            return;
        }
        paused = false;
        lastTickTime = System.nanoTime();
        hudController.reset();
        if (scoreController != null) {
            scoreController.reset();
        }
        primaryEntityController.clearAll();
        tickTask = scheduler.scheduleAtFixedRate(
                this::tick, 0, TICK_PERIOD_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        if (tickTask != null) {
            tickTask.cancel(false);
            tickTask = null;
        }
    }

    @Override
    public void shutdown() {
        stop();
        scheduler.shutdown();
    }

    private void tick() {
        if (paused) {
            lastTickTime = System.nanoTime();
            pushRenderFrame(true);
            return;
        }

        final long now = System.nanoTime();
        long deltaMs = (now - lastTickTime) / 1_000_000;
        lastTickTime = now;

        if (deltaMs > MAX_DELTA_MS) {
            deltaMs = MAX_DELTA_MS;
        }
        if (deltaMs <= 0) {
            return;
        }

        final long frameDeltaMs = deltaMs;
        entityControllers.forEach(ec -> ec.updateEntities(frameDeltaMs));
        collisionEngine.tick();
        if (scoreController != null) {
            scoreController.onTick(deltaMs);
        }
        pushRenderFrame(false);
    }

    private void pushRenderFrame(final boolean isPaused) {
        final List<Entity> entities = primaryEntityController.getEntities();
        final Plane plane = entities.stream()
                .filter(e -> e instanceof Plane)
                .map(e -> (Plane) e)
                .findFirst()
                .orElse(null);
        final List<EntityRenderData> collectibles = entities.stream()
                .filter(e -> e instanceof Collectible)
                .map(e -> new EntityRenderData(e.getPosition().getX(), e.getPosition().getY(), 0, "collectible"))
                .toList();
        final RenderState state = RenderState.builder()
                .plane(plane)
                .hud(hudController.buildSnapshot(plane, isPaused))
                .collectibles(collectibles)
                .build();
        notifyViews(v -> v.renderFrame(state));
    }

    private void notifyViews(final Consumer<GameView> action) {
        views.forEach(action);
    }

    @Override
    public void onInternalEvent(final InternalEvent evt, final Object data) {
        if (!(data instanceof final CollisionData collisionData)) {
            return;
        }
        if (primaryEntityController != null) {
            primaryEntityController.onInternalEvent(evt, collisionData);
        }
        switch (evt) {
            case PLANE_MISSILE_COLLISION -> {
                //notifyViews(v -> v.onPlaneHit(collisionData)); da implementare
                final Plane plane = (Plane) collisionData.getEntityB();
                if (!plane.isShieldActive()) {
                    // Gestisci danno al piano, es. riduci salute o simili
                    handleEvent(OutmaneuverEvent.GAME_OVER);
                }
            }
            case PLANE_COLLECTIBLE_COLLISION -> {
                if (collisionData.getEntityB() instanceof final Collectible collectible) {
                    hudController.onInternalEvent(InternalEvent.PLANE_COLLECTIBLE_COLLISION, collectible);
                    if (scoreController != null) {
                        scoreController.onInternalEvent(InternalEvent.PLANE_COLLECTIBLE_COLLISION, collectible);
                    }
                }
            }
            case MISSILE_MISSILE_COLLISION -> {
                // notifyViews(v -> v.onMissileCollision(collisionData)); da implementare
                if (scoreController != null) {
                    scoreController.onInternalEvent(InternalEvent.MISSILE_MISSILE_COLLISION, collisionData);
                }
            }
        }
    }
}
