package outmaneuver.controller;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.controller.impl.InputControllerImpl;
import outmaneuver.controller.impl.MasterControllerImpl;
import outmaneuver.controller.impl.RenderStateAssemblerImpl;
import outmaneuver.controller.impl.ScoreControllerImpl;
import outmaneuver.controller.event.Event;
import outmaneuver.model.session.Session;
import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.area.entity.plane.PlaneData;
import outmaneuver.model.area.entity.plane.PlaneImpl;
import outmaneuver.util.Vector2;
import outmaneuver.view.GameView;
import outmaneuver.view.RenderState;
import outmaneuver.controller.event.GameEvent;

class MasterControllerImplTest {

    private static final long TICK_WAIT_MS = 100;
    /**
     * Longer than one TICK_MS (16ms), so any tick already in flight when handleEvent() is
     * called has time to land before the test samples "during pause" state.
     */
    private static final long PAUSE_SETTLE_MS = 30;
    private static final long SHORT_SLEEP_MS = 50;
    private static final int PLANE_SPEED = 200;
    private static final int PLANE_TURN_RATE = 20;
    private static final double PAUSE_TEST_START_X = 400;
    private static final double PAUSE_TEST_START_Y = 300;
    private static final double RESUME_TEST_START_X = 200;
    private static final double RESUME_TEST_START_Y = 300;
    private static final double EPS_LOOSE = 1e-6;

    private Plane plane;
    private MasterControllerImpl master;
    private SpyView spyView;

    @BeforeEach
    void setUp() {
        plane = new PlaneImpl(new PlaneData("standard", PLANE_SPEED, 3, PLANE_TURN_RATE, "aircraft_standard", 0));
        spyView = new SpyView();
        master = new MasterControllerImpl();
        final List<Entity> sharedEntities = new ArrayList<>();
        final FakeEntityController entityCtrl = new FakeEntityController(sharedEntities);
        entityCtrl.spawnEntity(plane);
        master.addEntityController(entityCtrl);
        master.setSceneEntities(sharedEntities);
        master.setCollisionEngine(new CollisionEngine(master));
        master.setStateAssembler(new RenderStateAssemblerImpl());
        master.setScoreController(new ScoreControllerImpl(new Session(), () -> 16L));
        master.setEventController((evt, data) -> { });
        master.setInputController(new InputControllerImpl());
    }

    @Test
    void testPauseStopsMovement() throws InterruptedException {
        plane.setPosition(new Vector2(PAUSE_TEST_START_X, PAUSE_TEST_START_Y));

        master.attachView(spyView);
        master.start();
        Thread.sleep(SHORT_SLEEP_MS);
        spyView.getFrames().clear();

        master.handleEvent(GameEvent.PAUSED);
        // Let any tick already in flight on the game-loop thread land before sampling,
        // otherwise it can race with handleEvent() and sneak in one extra move.
        Thread.sleep(PAUSE_SETTLE_MS);
        final Vector2 posBefore = plane.getPosition();
        Thread.sleep(TICK_WAIT_MS);
        master.stop();
        final Vector2 posAfter = plane.getPosition();
        assertEquals(posBefore.getX(), posAfter.getX(), EPS_LOOSE,
                "Position should not change while paused");
    }

    @Test
    void testResumeResumesMovement() throws InterruptedException {
        plane.setPosition(new Vector2(RESUME_TEST_START_X, RESUME_TEST_START_Y));

        master.attachView(spyView);
        master.start();
        Thread.sleep(PAUSE_SETTLE_MS);
        master.handleEvent(GameEvent.PAUSED);
        Thread.sleep(PAUSE_SETTLE_MS);

        final Vector2 posBefore = plane.getPosition();
        master.handleEvent(GameEvent.PAUSED);
        Thread.sleep(SHORT_SLEEP_MS);
        master.stop();

        final Vector2 posAfter = plane.getPosition();
        assertTrue(posAfter.getX() > posBefore.getX(),
                "Position should advance after resume");
    }

    @Test
    void testStartStopCanBeCalledMultipleTimes() {
        master.attachView(spyView);
        master.start();
        master.stop();
        master.start();
        master.stop();
    }

    /**
     * Minimal EntityController double: advances any spawned plane along +X by
     * deltaMs on every updateEntities() call, so tests can observe movement
     * without depending on a concrete EntityControllerImpl subclass.
     */
    private static final class FakeEntityController implements EntityController {
        private final List<Entity> entities;
        private Plane plane;

        FakeEntityController(final List<Entity> entities) {
            this.entities = entities;
        }

        @Override
        public void updateEntities(final long deltaMs) {
            entities.stream()
                    .filter(e -> e instanceof Plane)
                    .map(e -> (Plane) e)
                    .forEach(p -> p.setPosition(p.getPosition().add(new Vector2(deltaMs, 0))));
        }

        @Override
        public void clearAll() {
            // Mirrors PlaneControllerImpl: removeAll() wipes the shared list, so the
            // plane has to be re-seeded here, not merely filtered back into existence.
            if (plane != null) {
                spawnEntity(plane);
            }
        }

        @Override
        public void spawnEntity(final Entity entity) {
            if (entity instanceof final Plane p) {
                plane = p;
            }
            entities.add(entity);
        }

        @Override
        public void removeEntity(final Entity entity) {
            entities.remove(entity);
        }

        @Override
        public void removeAll() {
            entities.clear();
        }

        @Override
        public List<Entity> getEntities() {
            return List.copyOf(entities);
        }

        @Override
        public void onInternalEvent(final Event evt, final Object data) {
        }
    }

    private static final class SpyView implements GameView {
        private final List<RenderState> frames = new ArrayList<>();

        List<RenderState> getFrames() {
            return frames;
        }

        @Override
        public void renderFrame(final RenderState state) {
            frames.add(state);
        }

        @Override
        public int getWidth() {
            return 0;
        }

        @Override
        public int getHeight() {
            return 0;
        }
    }
}
