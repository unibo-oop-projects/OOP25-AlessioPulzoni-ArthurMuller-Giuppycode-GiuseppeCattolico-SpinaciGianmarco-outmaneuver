package outmaneuver.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.controller.impl.HudControllerImpl;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.area.entity.plane.PlaneData;
import outmaneuver.model.area.entity.plane.PlaneImpl;
import outmaneuver.model.area.entity.collectibles.StarCollectible;
import outmaneuver.util.Vector2;
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
        final StarCollectible star = new StarCollectible(Vector2.ZERO, 10);
        hud.onInternalEvent(InternalEvent.PLANE_COLLECTIBLE_COLLISION, star);
        hud.onInternalEvent(InternalEvent.PLANE_COLLECTIBLE_COLLISION, star);
        assertEquals(2, hud.buildSnapshot(plane, false).stars());
    }

    @Test
    void resetClearsStars() {
        hud.onInternalEvent(InternalEvent.PLANE_COLLECTIBLE_COLLISION, new StarCollectible(Vector2.ZERO, 10));
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
