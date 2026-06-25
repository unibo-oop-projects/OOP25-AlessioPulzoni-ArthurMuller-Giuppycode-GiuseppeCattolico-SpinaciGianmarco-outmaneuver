package outmaneuver.controller;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.controller.impl.InputControllerImpl;
import outmaneuver.controller.impl.MasterControllerImpl;
import outmaneuver.controller.impl.RenderStateAssemblerImpl;
import outmaneuver.controller.impl.SessionState;
import outmaneuver.controller.event.Event;
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
    /** Longer than one TICK_MS (16ms), so any tick already in flight when handleEvent() is
     * called has time to land before the test samples "during pause" state. */
    private static final long PAUSE_SETTLE_MS = 30;

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

    private Plane plane;
    private FakeEntityController entityCtrl;
    private MasterControllerImpl master;
    private SpyView spyView;
    private List<Entity> sharedEntities;

    private static class SpyView implements GameView {
        final List<RenderState> frames = new ArrayList<>();

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

    @BeforeEach
    void setUp() {
        plane = new PlaneImpl(new PlaneData("standard", 200, 3, 20, "aircraft_standard", 0));
        spyView = new SpyView();
        master = new MasterControllerImpl();
        sharedEntities = new ArrayList<>();
        entityCtrl = new FakeEntityController(sharedEntities);
        entityCtrl.spawnEntity(plane);
        master.addEntityController(entityCtrl);
        master.setSceneEntities(sharedEntities);
        master.setCollisionEngine(new CollisionEngine(master));
        master.setStateAssembler(new RenderStateAssemblerImpl());
        master.setSessionState(new SessionState());
        master.setEventController((evt, data) -> { });
        master.setInputController(new InputControllerImpl());
    }

    @Test
    void testAttachViewAndTickProducesFrames() throws InterruptedException {
        master.attachView(spyView);
        master.start();
        Thread.sleep(TICK_WAIT_MS);
        master.stop();
        assertFalse(spyView.frames.isEmpty(), "View should receive frames after starting");
    }

    @Test
    void testFrameContainsPlaneData() throws InterruptedException {
        master.attachView(spyView);
        master.start();
        Thread.sleep(TICK_WAIT_MS);
        master.stop();
        final RenderState state = spyView.frames.get(0);
        assertNotNull(state.getPlane());
    }

    @Test
    void testPauseStopsMovement() throws InterruptedException {
        plane.setPosition(new Vector2(400, 300));

        master.attachView(spyView);
        master.start();
        Thread.sleep(50);
        spyView.frames.clear();

        master.handleEvent(GameEvent.PAUSED);
        // Let any tick already in flight on the game-loop thread land before sampling,
        // otherwise it can race with handleEvent() and sneak in one extra move.
        Thread.sleep(PAUSE_SETTLE_MS);
        final Vector2 posBefore = plane.getPosition();
        Thread.sleep(TICK_WAIT_MS);
        master.stop();
        final Vector2 posAfter = plane.getPosition();
        assertEquals(posBefore.getX(), posAfter.getX(), 1e-6,
                "Position should not change while paused");
    }

    @Test
    void testResumeResumesMovement() throws InterruptedException {
        plane.setPosition(new Vector2(200, 300));

        master.attachView(spyView);
        master.start();
        Thread.sleep(30);
        master.handleEvent(GameEvent.PAUSED);
        Thread.sleep(30);

        final Vector2 posBefore = plane.getPosition();
        master.handleEvent(GameEvent.PAUSED);
        Thread.sleep(50);
        master.stop();

        final Vector2 posAfter = plane.getPosition();
        assertTrue(posAfter.getX() > posBefore.getX(),
                "Position should advance after resume");
    }

    @Test
    void testPauseAndResumeToggle() throws InterruptedException {
        master.attachView(spyView);
        master.start();
        Thread.sleep(30);

        spyView.frames.clear();
        master.handleEvent(GameEvent.PAUSED);
        Thread.sleep(PAUSE_SETTLE_MS);
        final Vector2 posDuringPause1 = plane.getPosition();
        Thread.sleep(TICK_WAIT_MS);
        final Vector2 posDuringPause2 = plane.getPosition();
        assertFalse(spyView.frames.isEmpty(), "Frames should still arrive while paused (HUD overlay)");
        assertEquals(posDuringPause1.getX(), posDuringPause2.getX(), 1e-9,
                "Plane should not move while paused");

        spyView.frames.clear();
        master.handleEvent(GameEvent.PAUSED);
        Thread.sleep(TICK_WAIT_MS);
        master.stop();
        assertTrue(spyView.frames.size() >= 2, "Frames should resume after resume");
    }

    @Test
    void testStartStopCanBeCalledMultipleTimes() {
        master.attachView(spyView);
        master.start();
        master.stop();
        master.start();
        master.stop();
    }
}
