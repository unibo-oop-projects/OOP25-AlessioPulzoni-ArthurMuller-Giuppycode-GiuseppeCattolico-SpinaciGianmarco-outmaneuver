package outmaneuver.controller.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import outmaneuver.controller.CollisionEngine;
import outmaneuver.controller.event.EffectEvent;
import outmaneuver.controller.event.Event;
import outmaneuver.controller.EntityController;
import outmaneuver.controller.event.GameEvent;
import outmaneuver.controller.event.InternalEventListener;
import outmaneuver.controller.event.CollisionEvent;
import outmaneuver.controller.InputController;
import outmaneuver.controller.MasterController;
import outmaneuver.controller.RenderStateAssembler;
import outmaneuver.controller.ScoreController;
import outmaneuver.model.profile.PlayerProfile;
import outmaneuver.model.area.collision.CollisionData;
import outmaneuver.model.area.entity.Entity;
import outmaneuver.util.Vector2;

import outmaneuver.view.GameView;
import outmaneuver.view.RenderState;

/**
 * Default {@link MasterController} implementation. Wires together the entity
 * controllers, collision engine, score controller and state assembler, and drives
 * them from a dedicated game-loop thread running at a fixed tick rate.
 */
public final class MasterControllerImpl implements MasterController {

    private static final long TICK_MS = 16;
    private static final int GAME_OVER_DELAY_TICKS = 80;

    private final List<GameView> views = new ArrayList<>();
    private final List<EntityController> entityControllers = new ArrayList<>();
    private List<Entity> sceneEntities = List.of();
    private ScoreController scoreController;
    private InputController inputController;
    private InternalEventListener eventController;
    private RenderStateAssembler stateAssembler;
    private CollisionEngine collisionEngine;
    private Thread gameLoopThread;
    private volatile boolean running;
    private volatile GameEvent gameState;
    private final AtomicInteger gameOverDelayTicks = new AtomicInteger(-1);
    private PlayerProfile playerProfile;
    private final List<Vector2> pendingCollisionPoints = new ArrayList<>();
    private Runnable onGameOver;
    private Runnable onPause;
    private Runnable onResume;

    /** Creates a new controller starting in the paused state. */
    public MasterControllerImpl() {
        this.gameState = GameEvent.PAUSED;
    }

    /**
     * Returns the fixed duration of a single game-loop tick.
     *
     * @return the tick duration in milliseconds
     */
    public long getTickMs() {
        return TICK_MS;
    }

    /**
     * Registers the callback invoked when the game transitions to the game-over state.
     *
     * @param onGameOver the callback to invoke on game over
     */
    public void setOnGameOver(final Runnable onGameOver) {
        this.onGameOver = Objects.requireNonNull(onGameOver);
    }

    /**
     * Registers the callback invoked when the game transitions to the paused state.
     *
     * @param onPause the callback to invoke on pause
     */
    public void setOnPause(final Runnable onPause) {
        this.onPause = Objects.requireNonNull(onPause);
    }

    /**
     * Registers the callback invoked when the game resumes from the paused state.
     *
     * @param onResume the callback to invoke on resume
     */
    public void setOnResume(final Runnable onResume) {
        this.onResume = Objects.requireNonNull(onResume);
    }

    /**
     * Sets the input controller polled for the plane's turn direction and reset on
     * {@link #start()}.
     *
     * @param inputController the input controller to use
     */
    public void setInputController(final InputController inputController) {
        this.inputController = Objects.requireNonNull(inputController, "inputController must not be null");
    }

    /**
     * Sets the listener notified of internal events (collisions, effects) produced
     * during the game loop, in addition to this controller's own handling.
     *
     * @param eventController the event listener to notify
     */
    public void setEventController(final InternalEventListener eventController) {
        this.eventController = Objects.requireNonNull(eventController, "eventController must not be null");
    }

    /**
     * Sets the assembler used to build the {@link RenderState} sent to views each frame.
     *
     * @param stateAssembler the render state assembler to use
     */
    public void setStateAssembler(final RenderStateAssembler stateAssembler) {
        this.stateAssembler = Objects.requireNonNull(stateAssembler, "stateAssembler must not be null");
    }

    /**
     * Sets the entities of the active scene that are passed to the state assembler
     * each frame.
     *
     * @param sceneEntities the entities composing the current scene
     */
    public void setSceneEntities(final List<Entity> sceneEntities) {
        this.sceneEntities = Objects.requireNonNull(sceneEntities, "sceneEntities must not be null");
    }

    /**
     * Registers an entity controller to be driven by the game loop and looked up via
     * {@link #getEntityController(Class)}.
     *
     * @param entityController the entity controller to add
     */
    public void addEntityController(final EntityController entityController) {
        entityControllers.add(Objects.requireNonNull(entityController, "entityController must not be null"));
    }

    /**
     * Looks up the registered entity controller of the given concrete type.
     *
     * @param <T> the entity controller type
     * @param type the class of the entity controller to look up
     * @return the matching entity controller, or {@link Optional#empty()} if none was registered
     */
    public <T extends EntityController> Optional<T> getEntityController(final Class<T> type) {
        return entityControllers.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .findFirst();
    }

    /**
     * Sets the collision engine used to detect collisions each tick. Can only be set once.
     *
     * @param collisionEngine the collision engine to use
     * @throws IllegalStateException if a collision engine has already been set
     */
    public void setCollisionEngine(final CollisionEngine collisionEngine) {
        if (this.collisionEngine != null) {
            throw new IllegalStateException("collisionEngine already set");
        }
        this.collisionEngine = Objects.requireNonNull(collisionEngine, "collisionEngine must not be null");
    }

    /**
     * Sets the controller tracking score, elapsed time and gameplay modifiers.
     *
     * @param scoreController the score controller to use
     */
    public void setScoreController(final ScoreController scoreController) {
        this.scoreController = Objects.requireNonNull(scoreController, "scoreController must not be null");
    }

    /**
     * Sets the player profile updated with coins and saved scores when a game ends.
     *
     * @param playerProfile the player profile to use
     */
    public void setPlayerProfile(final PlayerProfile playerProfile) {
        this.playerProfile = Objects.requireNonNull(playerProfile, "playerProfile must not be null");
    }

    @Override
    @SuppressFBWarnings(
            value = "DM_EXIT",
            justification = "QUIT_APPLICATION must terminate the JVM after a clean shutdown")
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
            case GAME_OVER -> gameOverDelayTicks.compareAndSet(-1, GAME_OVER_DELAY_TICKS);
            default -> {
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
        Objects.requireNonNull(stateAssembler, "stateAssembler must be set before start()");
        Objects.requireNonNull(scoreController, "scoreController must be set before start()");
        if (running) {
            return;
        }
        gameState = GameEvent.RUNNING;
        stateAssembler.reset();
        scoreController.reset();
        gameOverDelayTicks.set(-1);
        pendingCollisionPoints.clear();
        inputController.reset();
        entityControllers.forEach(EntityController::removeAll);
        entityControllers.forEach(EntityController::clearAll);
        running = true;
        gameLoopThread = new Thread(this::gameLoop, "game-loop");
        gameLoopThread.setDaemon(true);
        gameLoopThread.start();
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public void shutdown() {
        stop();
        if (gameLoopThread != null) {
            gameLoopThread.interrupt();
            try {
                gameLoopThread.join(1000);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            gameLoopThread = null;
        }
    }

    private void gameLoop() {
        while (running && !Thread.currentThread().isInterrupted()) {
            final long frameStart = System.nanoTime();

            if (gameState == GameEvent.RUNNING && gameOverDelayTicks.get() < 0) {
                updateFrame();
            }

            if (gameOverDelayTicks.get() > 0 && gameOverDelayTicks.decrementAndGet() == 0) {
                gameState = GameEvent.GAME_OVER;
                running = false;
                final int finalScore = scoreController.getScore();
                if (finalScore > 0 && playerProfile != null) {
                    playerProfile.addCoins(finalScore);
                }
                if (playerProfile != null) {
                    playerProfile.saveScore(finalScore, playerProfile.getPlayerName());
                }
                if (onGameOver != null) {
                    onGameOver.run();
                }
            }

            renderFrame();
            pendingCollisionPoints.clear();

            final long elapsedMs = (System.nanoTime() - frameStart) / 1_000_000;
            final long sleepMs = TICK_MS - elapsedMs;
            if (sleepMs > 0) {
                try {
                    Thread.sleep(sleepMs);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void updateFrame() {
        entityControllers.forEach(ec -> ec.updateEntities(TICK_MS));
        collisionEngine.tick();
        scoreController.onTick();
    }

    private void renderFrame() {
        final boolean paused = gameState == GameEvent.PAUSED;
        final RenderState state = stateAssembler.assemble(
                sceneEntities,
                paused,
                scoreController.getElapsedMs(),
                scoreController.getStars(),
                scoreController.getSpeedMultiplier(),
                scoreController.isShieldActive(),
                pendingCollisionPoints);
        notifyViews(v -> v.renderFrame(state));
    }

    private void notifyViews(final Consumer<GameView> action) {
        views.forEach(action);
    }

    @Override
    public void onInternalEvent(final Event evt, final Object data) {
        if (evt instanceof EffectEvent) {
            entityControllers.forEach(ec -> ec.onInternalEvent(evt, data));
        }
        if (data instanceof final CollisionData collisionData
                && (evt == CollisionEvent.MISSILE_MISSILE_COLLISION
                        || evt == CollisionEvent.PLANE_MISSILE_COLLISION)) {
            pendingCollisionPoints.add(collisionData.getCollisionPoint());
        }
        if (eventController != null) {
            eventController.onInternalEvent(evt, data);
        }
    }
}
