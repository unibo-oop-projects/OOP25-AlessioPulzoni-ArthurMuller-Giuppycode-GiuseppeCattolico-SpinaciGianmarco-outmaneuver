package outmaneuver.controller;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.controller.impl.MasterControllerImpl;
import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.area.entity.plane.PlaneData;
import outmaneuver.model.area.entity.plane.PlaneImpl;
import outmaneuver.util.Vector2;
import outmaneuver.view.EntityRenderData;
import outmaneuver.view.GameView;
import outmaneuver.view.HudSnapshot;
import outmaneuver.view.RenderState;

class MasterControllerImplTest {

    private static final long TICK_WAIT_MS = 100;

    /**
     * Minimal EntityController double: advances any spawned plane along +X by
     * deltaMs on every updateEntities() call, so tests can observe movement
     * without depending on a concrete EntityControllerImpl subclass.
     */
    private static final class FakeEntityController implements EntityController {
        private final List<Entity> entities;

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
            entities.removeIf(e -> !(e instanceof Plane));
        }

        @Override
        public void spawnEntity(final Entity entity) {
            entities.add(entity);
        }

        @Override
        public void removeEntity(final Entity entity) {
            entities.remove(entity);
        }

        @Override
        public List<Entity> getEntities() {
            return List.copyOf(entities);
        }

        @Override
        public void onInternalEvent(final CollisionEvent evt, final Object data) {
        }
    }

    private static final class StubRenderStateAssembler implements RenderStateAssembler {
        @Override
        public RenderState assemble(final List<Entity> entities, final boolean paused) {
            final Plane plane = entities.stream()
                    .filter(e -> e instanceof Plane)
                    .map(e -> (Plane) e)
                    .findFirst()
                    .orElse(null);
            final EntityRenderData planeData = plane != null
                    ? new EntityRenderData(plane.getPosition().getX(), plane.getPosition().getY(),
                            plane.getDirection(), plane.getStats().getSpriteId())
                    : null;
            return RenderState.builder()
                    .planeData(planeData)
                    .hud(new HudSnapshot(0, 0, false, paused, 0))
                    .build();
        }

        @Override
        public void reset() {
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
        master.setStateAssembler(new StubRenderStateAssembler());
        master.setEventController((evt, data) -> { });
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

        master.handleEvent(OutmaneuverEvent.TOGGLE_PAUSE);
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
