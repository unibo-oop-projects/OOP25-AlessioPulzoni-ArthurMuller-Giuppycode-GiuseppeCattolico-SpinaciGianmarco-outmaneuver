package outmaneuver.controller.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import outmaneuver.controller.EntityController;
import outmaneuver.controller.InternalEvent;
import outmaneuver.controller.MasterController;
import outmaneuver.controller.OutmaneuverEvent;
import outmaneuver.controller.event.InternalEventListener;
import outmaneuver.view.GameView;
import outmaneuver.view.RenderState;

public final class MasterControllerImpl implements MasterController, InternalEventListener {

    private static final long TICK_PERIOD_MS = 16;
    private static final long MAX_DELTA_MS = 50;

    private final List<GameView> views = new ArrayList<>();
    private EntityController entityController;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> tickTask;
    private volatile boolean paused;
    private long lastTickTime;

    public MasterControllerImpl() {
        this.paused = false;
    }

    public void setEntityController(final EntityController entityController) {
        this.entityController = Objects.requireNonNull(entityController, "entityController must not be null");
    }

    @Override
    public void handleEvent(final OutmaneuverEvent event) {
        switch (event) {
            case PAUSE_GAME -> paused = true;
            case RESUME_GAME -> {
                paused = false;
                lastTickTime = System.nanoTime();
            }
            case QUIT_APPLICATION -> {
                stop();
                System.exit(0);
            }
        }
    }

    @Override
    public void attachView(final GameView view) {
        views.add(Objects.requireNonNull(view, "view must not be null"));
    }

    @Override
    public void start() {
        if (tickTask != null && !tickTask.isCancelled()) {
            return;
        }
        lastTickTime = System.nanoTime();
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

    private void tick() {
        if (paused) {
            lastTickTime = System.nanoTime();
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

        final RenderState state = RenderState.builder()
                .plane(entityController.getPlane())
                .build();

        notifyViews(v -> v.renderFrame(state));
    }

    private void notifyViews(final Consumer<GameView> action) {
        views.forEach(action);
    }

    @Override
    public void onInternalEvent(final InternalEvent evt, final Object data) {
        // No events to handle yet
    }
}
