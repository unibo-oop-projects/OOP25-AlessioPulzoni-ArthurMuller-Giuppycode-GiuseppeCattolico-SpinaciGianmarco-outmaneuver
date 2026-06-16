package outmaneuver.model.collectibles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import outmaneuver.model.area.entity.collectibles.ShieldPowerUp;
import outmaneuver.model.area.entity.collectibles.SpeedBoost;
import outmaneuver.model.area.entity.collectibles.StarCollectible;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.session.GameSession;
import outmaneuver.model.session.GameState;

class CollectiblesTest {

    // SpeedBoost

    @Test
    void speedBoostAppliesMultiplierToPlane() {
        final Plane plane = mock(Plane.class);
        final GameSession session = new GameSession();
        new SpeedBoost(2.0, 3000).apply(plane, session);
        verify(plane).applySpeedMultiplier(2.0, 3000);
    }

    @Test
    void speedBoostThrowsOnNonPositiveFactor() {
        assertThrows(IllegalArgumentException.class, () -> new SpeedBoost(0, 1000));
        assertThrows(IllegalArgumentException.class, () -> new SpeedBoost(-1, 1000));
    }

    @Test
    void speedBoostThrowsOnNonPositiveDuration() {
        assertThrows(IllegalArgumentException.class, () -> new SpeedBoost(2.0, 0));
        assertThrows(IllegalArgumentException.class, () -> new SpeedBoost(2.0, -1));
    }

    // StarCollectible

    @Test
    void starCollectibleIncrementsSessionScore() {
        final Plane plane = mock(Plane.class);
        final GameSession session = new GameSession();
        new StarCollectible(50).apply(plane, session);
        assertEquals(50, session.getScore());
    }

    @Test
    void starCollectibleAccumulatesScore() {
        final Plane plane = mock(Plane.class);
        final GameSession session = new GameSession();
        new StarCollectible(30).apply(plane, session);
        new StarCollectible(20).apply(plane, session);
        assertEquals(50, session.getScore());
    }

    @Test
    void starCollectibleThrowsOnNonPositiveValue() {
        assertThrows(IllegalArgumentException.class, () -> new StarCollectible(0));
        assertThrows(IllegalArgumentException.class, () -> new StarCollectible(-5));
    }

    // ShieldPowerUp

    @Test
    void shieldPowerUpActivatesShield() {
        final Plane plane = mock(Plane.class);
        final GameSession session = new GameSession();
        new ShieldPowerUp(5000).apply(plane, session);
        verify(plane).activateShield();
    }

    @Test
    void shieldPowerUpDeactivatesAfterDuration() throws InterruptedException {
        final Plane plane = mock(Plane.class);
        final GameSession session = new GameSession();
        new ShieldPowerUp(80).apply(plane, session);
        Thread.sleep(150);
        verify(plane).deactivateShield();
    }

    @Test
    void shieldPowerUpThrowsOnNonPositiveDuration() {
        assertThrows(IllegalArgumentException.class, () -> new ShieldPowerUp(0));
        assertThrows(IllegalArgumentException.class, () -> new ShieldPowerUp(-1));
    }
}
