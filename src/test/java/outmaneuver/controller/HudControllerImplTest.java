package outmaneuver.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.controller.impl.HudControllerImpl;
import outmaneuver.model.area.Plane;
import outmaneuver.model.area.PlaneImpl;
import outmaneuver.model.area.PlaneData;
import outmaneuver.view.HudSnapshot;

class HudControllerImplTest {

    private HudControllerImpl hud;
    private Plane plane;

    @BeforeEach
    void setUp() {
        hud = new HudControllerImpl();
        plane = new PlaneImpl(new PlaneData("standard", 200, 3, 20, "aircraft_standard", 0));
    }

    @Test
    void snapshotReflectsPauseState() {
        assertTrue(hud.buildSnapshot(plane, true).paused());
        assertFalse(hud.buildSnapshot(plane, false).paused());
    }

    @Test
    void snapshotSpeedMatchesPlane() {
        final HudSnapshot snap = hud.buildSnapshot(plane, false);
        assertEquals(plane.getEffectiveSpeed(), snap.speed());
    }

    @Test
    void starsIncrementOnStarCollected() {
        hud.onInternalEvent(InternalEvent.STAR_COLLECTED, null);
        hud.onInternalEvent(InternalEvent.STAR_COLLECTED, null);
        assertEquals(2, hud.buildSnapshot(plane, false).stars());
    }

    @Test
    void resetClearsStars() {
        hud.onInternalEvent(InternalEvent.STAR_COLLECTED, null);
        hud.reset();
        assertEquals(0, hud.buildSnapshot(plane, false).stars());
    }

    @Test
    void elapsedTimeGrowsAfterReset() throws InterruptedException {
        hud.reset();
        Thread.sleep(50);
        assertTrue(hud.buildSnapshot(plane, false).elapsedMs() >= 50);
    }
}
