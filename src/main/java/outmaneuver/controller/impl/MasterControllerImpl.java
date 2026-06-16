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
import outmaneuver.controller.event.InternalEventListener;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.area.entity.collectibles.Collectible;
import outmaneuver.view.EntityRenderData;
import outmaneuver.view.GameView;
import outmaneuver.view.RenderState;

public final class MasterControllerImpl implements MasterController, InternalEventListener {

    private static final long TICK_PERIOD_MS = 16;
    private static final long MAX_DELTA_MS = 50;

    private final List<GameView> views = new ArrayList<>();
    private final HudController hudController;
    private ScoreController scoreController;
    private EntityController entityController;
    private CollisionEngine collisionEngine;
    private CollectibleSpawner collectibleSpawner;
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

    public void setEntityController(final EntityController entityController) {
        if (this.entityController != null) {
            throw new IllegalStateException("entityController already set");
        }
        this.entityController = Objects.requireNonNull(entityController, "entityController must not be null");
        this.collectibleSpawner = new CollectibleSpawner(this.entityController);
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
    }

    @Override
    public void start() {
        Objects.requireNonNull(entityController, "entityController must be set before start()");
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

        entityController.updateEntities(deltaMs);
        collectibleSpawner.tick(deltaMs, entityController.getPlane());
        collisionEngine.tick();
        if (scoreController != null) {
            scoreController.onTick(deltaMs);
        }
        pushRenderFrame(false);
    }

    private void pushRenderFrame(final boolean isPaused) {
        final Plane plane = entityController.getPlane();
        final List<EntityRenderData> collectibles = entityController.getEntities().stream()
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
        switch (evt) {
            case PLANE_MISSILE_COLLISION -> {
                //notifyViews(v -> v.onPlaneHit((CollisionData) data)); da implementare
            }
            case PLANE_COLLECTIBLE_COLLISION -> {
                hudController.onInternalEvent(InternalEvent.PLANE_COLLECTIBLE_COLLISION, data);
                if (scoreController != null) {
                    scoreController.onInternalEvent(InternalEvent.PLANE_COLLECTIBLE_COLLISION, data);
                }
            }
            case MISSILE_MISSILE_COLLISION -> {
                // notifyViews(v -> v.onMissileCollision((CollisionData) data)); da implementare
                if (scoreController != null) {
                    scoreController.onInternalEvent(InternalEvent.MISSILE_MISSILE_COLLISION, data);
                }
            }
        }
    }
}
