package outmaneuver.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.controller.event.CollisionEvent;
import outmaneuver.controller.impl.HudControllerImpl;
import outmaneuver.model.area.collision.CollisionData;
import outmaneuver.model.area.entity.collectibles.StarCollectible;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.area.entity.plane.PlaneData;
import outmaneuver.model.area.entity.plane.PlaneImpl;
import outmaneuver.util.Vector2;

class HudControllerImplTest {

    private HudControllerImpl hud;
    private Plane plane;

    @BeforeEach
    void setUp() {
        hud = new HudControllerImpl();
        plane = new PlaneImpl(new PlaneData("standard", 200, 3, 20, "aircraft_standard", 0));
    }

    @Test
    void elapsedMsAccumulatesOnTick() {
        hud.onTick(16);
        hud.onTick(34);
        assertEquals(50, hud.getElapsedMs());
    }

    @Test
    void shieldActiveReflectsSetter() {
        assertFalse(hud.isShieldActive());
        hud.setShieldActive(true);
        assertTrue(hud.isShieldActive());
        hud.setShieldActive(false);
        assertFalse(hud.isShieldActive());
    }

    @Test
    void speedMultiplierDefaultsToOneAndReflectsSetter() {
        assertEquals(1.0, hud.getSpeedMultiplier());
        hud.setSpeedMultiplier(2.0);
        assertEquals(2.0, hud.getSpeedMultiplier());
    }

    @Test
    void starsIncrementOnStarCollectibleCollision() {
        final StarCollectible star = new StarCollectible(Vector2.ZERO, 10);
        final CollisionData data = new CollisionData(plane, star, Vector2.ZERO);

        hud.onInternalEvent(CollisionEvent.PLANE_COLLECTIBLE_COLLISION, data);
        hud.onInternalEvent(CollisionEvent.PLANE_COLLECTIBLE_COLLISION, data);

        assertEquals(2, hud.getStars());
    }

    @Test
    void starsDoNotIncrementOnUnrelatedEvent() {
        hud.onInternalEvent(CollisionEvent.MISSILE_MISSILE_COLLISION,
                new CollisionData(plane, plane, Vector2.ZERO));
        assertEquals(0, hud.getStars());
    }

    @Test
    void resetClearsAllState() {
        hud.onTick(100);
        hud.setShieldActive(true);
        hud.setSpeedMultiplier(3.0);
        hud.onInternalEvent(CollisionEvent.PLANE_COLLECTIBLE_COLLISION,
                new CollisionData(plane, new StarCollectible(Vector2.ZERO, 10), Vector2.ZERO));

        hud.reset();

        assertEquals(0L, hud.getElapsedMs());
        assertFalse(hud.isShieldActive());
        assertEquals(1.0, hud.getSpeedMultiplier());
        assertEquals(0, hud.getStars());
    }
}
