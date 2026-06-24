package outmaneuver.controller.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import outmaneuver.controller.CollisionEngine;
import outmaneuver.controller.event.EffectEvent;
import outmaneuver.controller.event.Event;
import outmaneuver.controller.EntityController;
import outmaneuver.controller.GameEventController;
import outmaneuver.controller.event.GameEvent;
import outmaneuver.controller.HudController;
import outmaneuver.controller.MasterController;
import outmaneuver.controller.RenderStateAssembler;
import outmaneuver.controller.ScoreController;
import outmaneuver.model.area.entity.Entity;

import outmaneuver.view.GameView;
import outmaneuver.view.RenderState;

public final class MasterControllerImpl implements MasterController {

    private static final long TICK_MS = 16;

    private final List<GameView> views = new ArrayList<>();
    private final List<EntityController> entityControllers = new ArrayList<>();
    private List<Entity> sceneEntities = List.of();
    private HudController hudController;
    private ScoreController scoreController;
    private GameEventController eventController;
    private RenderStateAssembler stateAssembler;
    private CollisionEngine collisionEngine;
    private Thread gameLoopThread;
    private volatile boolean running;
    private volatile GameEvent gameState;
    private Runnable onGameOver;
    private Runnable onPause;
    private Runnable onResume;

    public MasterControllerImpl() {
        this.gameState = GameEvent.PAUSED;
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

    public void setEventController(final GameEventController eventController) {
        this.eventController = Objects.requireNonNull(eventController, "eventController must not be null");
    }

    public void setHudController(final HudController hudController) {
        this.hudController = Objects.requireNonNull(hudController, "hudController must not be null");
    }

    public void setStateAssembler(final RenderStateAssembler stateAssembler) {
        this.stateAssembler = Objects.requireNonNull(stateAssembler, "stateAssembler must not be null");
    }

    public void setSceneEntities(final List<Entity> sceneEntities) {
        this.sceneEntities = Objects.requireNonNull(sceneEntities, "sceneEntities must not be null");
    }

    public void addEntityController(final EntityController entityController) {
        entityControllers.add(Objects.requireNonNull(entityController, "entityController must not be null"));
    }

    public <T extends EntityController> Optional<T> getEntityController(final Class<T> type) {
        return entityControllers.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .findFirst();
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
    public void handleEvent(final GameEvent event) {
        switch (event) {
            case PAUSED -> {
                if (gameState == GameEvent.RUNNING) {
                    gameState = GameEvent.PAUSED;
                    if (onPause != null) {
                        onPause.run();
                    }
                } else if (gameState == GameEvent.PAUSED) {
                    gameState = GameEvent.RUNNING;
                    if (onResume != null) {
                        onResume.run();
                    }
                }
            }
            case QUIT_APPLICATION -> {
                shutdown();
                System.exit(0);
            }
            case GAME_OVER -> {
                gameState = GameEvent.GAME_OVER;
                stop();
                if (onGameOver != null) {
                    onGameOver.run();
                }
            }
            default -> { }
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
        Objects.requireNonNull(stateAssembler, "stateAssembler must be set before start()");
        Objects.requireNonNull(eventController, "eventController must be set before start()");
        Objects.requireNonNull(hudController, "hudController must be set before start()");
        if (running) {
            return;
        }
        gameState = GameEvent.RUNNING;
        hudController.reset();
        if (scoreController != null) {
            scoreController.reset();
        }
        entityControllers.forEach(EntityController::clearAll);
        running = true;
        gameLoopThread = new Thread(this::gameLoop, "game-loop");
        gameLoopThread.setDaemon(true);
        gameLoopThread.start();
    }

    /**
     * Non-blocking stop. Sets {@code running = false}; the game loop thread
     * exits after its current iteration completes. The controller can be
     * restarted via {@link #start()}.
     */
    @Override
    public void stop() {
        running = false;
    }

    /**
     * Terminal shutdown. Interrupts the game loop thread and waits for it to
     * terminate. The controller cannot be restarted after this call.
     */
    @Override
    public void shutdown() {
        stop();
        if (gameLoopThread != null) {
            gameLoopThread.interrupt();
            try {
                gameLoopThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            gameLoopThread = null;
        }
    }

    private void gameLoop() {
        while (running && !Thread.currentThread().isInterrupted()) {
            final long frameStart = System.nanoTime();

            if (gameState == GameEvent.RUNNING) {
                updateFrame();
            }

            renderFrame();

            final long elapsedMs = (System.nanoTime() - frameStart) / 1_000_000;
            final long sleepMs = TICK_MS - elapsedMs;
            if (sleepMs > 0) {
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void updateFrame() {
        entityControllers.forEach(ec -> ec.updateEntities(TICK_MS));
        collisionEngine.tick();
        if (scoreController != null) {
            scoreController.onTick(TICK_MS);
        }
        if (hudController != null) {
            hudController.onTick(TICK_MS);
        }
    }

    private void renderFrame() {
        final boolean paused = gameState == GameEvent.PAUSED;
        final RenderState state = stateAssembler.assemble(
                sceneEntities,
                paused,
                hudController.getElapsedMs(),
                hudController.getStars(),
                hudController.getSpeedMultiplier(),
                hudController.isShieldActive());
        notifyViews(v -> v.renderFrame(state));
    }

    private void notifyViews(final Consumer<GameView> action) {
        views.forEach(action);
    }

    @Override
    public void onInternalEvent(final Event evt, final Object data) {
        // do we need it?
        if (evt instanceof EffectEvent) {
            entityControllers.forEach(ec -> ec.onInternalEvent(evt, data));
        }
        if (eventController != null) {
            eventController.onInternalEvent(evt, data);
        }
        if (hudController != null) {
            hudController.onInternalEvent(evt, data);
        }
    }
}
