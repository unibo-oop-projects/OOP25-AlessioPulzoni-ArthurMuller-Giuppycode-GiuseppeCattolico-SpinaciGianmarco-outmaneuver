package outmaneuver.controller;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.controller.impl.EntityControllerImpl;
import outmaneuver.controller.impl.HudControllerImpl;
import outmaneuver.controller.impl.InputControllerImpl;
import outmaneuver.controller.impl.MasterControllerImpl;
import outmaneuver.model.area.Plane;
import outmaneuver.model.area.PlaneImpl;
import outmaneuver.model.area.StandardStats;
import outmaneuver.util.Vector2;
import outmaneuver.view.GameView;
import outmaneuver.view.RenderState;

class MasterControllerImplTest {

    private static final long TICK_WAIT_MS = 100;

    private Plane plane;
    private InputControllerImpl input;
    private EntityControllerImpl entityCtrl;
    private MasterControllerImpl master;
    private SpyView spyView;

    private static class SpyView implements GameView {
        final List<RenderState> frames = new ArrayList<>();

        @Override
        public void renderFrame(final RenderState state) {
            frames.add(state);
        }
    }

    @BeforeEach
    void setUp() {
        plane = new PlaneImpl(new StandardStats());
        input = new InputControllerImpl();
        spyView = new SpyView();
        master = new MasterControllerImpl(new HudControllerImpl());
        entityCtrl = new EntityControllerImpl(plane, input, master);
        master.setEntityController(entityCtrl);
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
        input.onKeyPressed(39);

        master.attachView(spyView);
        master.start();
        Thread.sleep(50);
        spyView.frames.clear();

        master.handleEvent(OutmaneuverEvent.TOGGLE_PAUSE);
        final Vector2 posBefore = plane.getPosition();
        Thread.sleep(TICK_WAIT_MS);
        final Vector2 posAfter = plane.getPosition();
        assertEquals(posBefore.getX(), posAfter.getX(), 1e-6,
                "Position should not change while paused");
    }

    @Test
    void testResumeResumesMovement() throws InterruptedException {
        plane.setPosition(new Vector2(200, 300));
        input.onKeyPressed(39);

        master.attachView(spyView);
        master.start();
        Thread.sleep(30);
        master.handleEvent(OutmaneuverEvent.TOGGLE_PAUSE);
        Thread.sleep(30);

        final Vector2 posBefore = plane.getPosition();
        master.handleEvent(OutmaneuverEvent.TOGGLE_PAUSE);
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
        master.handleEvent(OutmaneuverEvent.TOGGLE_PAUSE);
        final Vector2 posDuringPause1 = plane.getPosition();
        Thread.sleep(TICK_WAIT_MS);
        final Vector2 posDuringPause2 = plane.getPosition();
        assertFalse(spyView.frames.isEmpty(), "Frames should still arrive while paused (HUD overlay)");
        assertEquals(posDuringPause1.getX(), posDuringPause2.getX(), 1e-9,
                "Plane should not move while paused");

        spyView.frames.clear();
        master.handleEvent(OutmaneuverEvent.TOGGLE_PAUSE);
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
