package outmaneuver.model.collectibles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import outmaneuver.model.area.entity.collectibles.ShieldPowerUp;
import outmaneuver.model.area.entity.collectibles.SpeedBoost;
import outmaneuver.model.area.entity.collectibles.StarCollectible;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.session.GameSession;
import outmaneuver.util.Vector2;

class CollectiblesTest {

    // SpeedBoost

    @Test
    void speedBoostAppliesMultiplierToPlane() {
        final Plane plane = mock(Plane.class);
        final GameSession session = new GameSession();
        new SpeedBoost(Vector2.ZERO, 2.0, 3000L).apply(plane, session);
        verify(plane).applySpeedMultiplier(2.0, 3000);
    }

    @Test
    void speedBoostThrowsOnNonPositiveFactor() {
        assertThrows(IllegalArgumentException.class, () -> new SpeedBoost(Vector2.ZERO, 0, 1000L));
        assertThrows(IllegalArgumentException.class, () -> new SpeedBoost(Vector2.ZERO, -1, 1000L));
    }

    @Test
    void speedBoostThrowsOnNonPositiveDuration() {
        assertThrows(IllegalArgumentException.class, () -> new SpeedBoost(Vector2.ZERO, 2.0, 0L));
        assertThrows(IllegalArgumentException.class, () -> new SpeedBoost(Vector2.ZERO, 2.0, -1L));
    }

    // StarCollectible

    @Test
    void starCollectibleApplyDoesNotIncrementSessionScore() {
        final Plane plane = mock(Plane.class);
        final GameSession session = new GameSession();
        new StarCollectible(Vector2.ZERO, 50).apply(plane, session);
        assertEquals(0, session.getScore());
    }

    @Test
    void starCollectibleGetScoreValueReturnsConfiguredValue() {
        assertEquals(30, new StarCollectible(Vector2.ZERO, 30).getScoreValue());
        assertEquals(20, new StarCollectible(Vector2.ZERO, 20).getScoreValue());
    }

    @Test
    void starCollectibleThrowsOnNonPositiveValue() {
        assertThrows(IllegalArgumentException.class, () -> new StarCollectible(Vector2.ZERO, 0));
        assertThrows(IllegalArgumentException.class, () -> new StarCollectible(Vector2.ZERO, -5));
    }

    // ShieldPowerUp

    @Test
    void shieldPowerUpActivatesShield() {
        final Plane plane = mock(Plane.class);
        final GameSession session = new GameSession();
        new ShieldPowerUp(Vector2.ZERO, 5000L).apply(plane, session);
        verify(plane).activateShield();
    }

    @Test
    void shieldPowerUpDeactivatesAfterDuration() throws InterruptedException {
        final Plane plane = mock(Plane.class);
        final GameSession session = new GameSession();
        new ShieldPowerUp(Vector2.ZERO, 80L).apply(plane, session);
        Thread.sleep(150);
        verify(plane).deactivateShield();
    }

    @Test
    void shieldPowerUpThrowsOnNonPositiveDuration() {
        assertThrows(IllegalArgumentException.class, () -> new ShieldPowerUp(Vector2.ZERO, 0L));
        assertThrows(IllegalArgumentException.class, () -> new ShieldPowerUp(Vector2.ZERO, -1L));
    }
}
