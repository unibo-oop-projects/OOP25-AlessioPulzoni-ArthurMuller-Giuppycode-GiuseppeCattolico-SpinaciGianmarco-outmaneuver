package outmaneuver.model.collectibles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import outmaneuver.model.area.collision.CollisionLayer;
import outmaneuver.model.area.effect.Effect;
import outmaneuver.model.area.effect.EffectImpl;
import outmaneuver.model.area.effect.EffectType;
import outmaneuver.model.area.entity.collectibles.ShieldPowerUp;
import outmaneuver.model.area.entity.collectibles.SpeedBoost;
import outmaneuver.model.area.entity.collectibles.StarCollectible;
import outmaneuver.util.Vector2;

class CollectiblesTest {

    // SpeedBoost

    @Test
    void speedBoostExposesItsEffect() {
        final Effect effect = new EffectImpl(EffectType.SPEED_BOOST, 2.0, 3000L);
        final SpeedBoost boost = new SpeedBoost(Vector2.ZERO, effect);
        assertSame(effect, boost.getEffect());
        assertEquals("speed", boost.getCollectibleType());
    }

    @Test
    void speedBoostThrowsOnNullEffect() {
        assertThrows(NullPointerException.class, () -> new SpeedBoost(Vector2.ZERO, null));
    }

    // StarCollectible

    @Test
    void starCollectibleHasNoEffect() {
        assertNull(new StarCollectible(Vector2.ZERO, 50).getEffect());
    }

    @Test
    void starCollectibleGetScoreValueReturnsConfiguredValue() {
        assertEquals(30, new StarCollectible(Vector2.ZERO, 30).getScoreValue());
        assertEquals(20, new StarCollectible(Vector2.ZERO, 20).getScoreValue());
    }

    @Test
    void starCollectibleGetCollectibleTypeIsStar() {
        assertEquals("star", new StarCollectible(Vector2.ZERO, 10).getCollectibleType());
    }

    @Test
    void starCollectibleThrowsOnNonPositiveValue() {
        assertThrows(IllegalArgumentException.class, () -> new StarCollectible(Vector2.ZERO, 0));
        assertThrows(IllegalArgumentException.class, () -> new StarCollectible(Vector2.ZERO, -5));
    }

    // ShieldPowerUp

    @Test
    void shieldPowerUpExposesItsEffect() {
        final Effect effect = new EffectImpl(EffectType.SHIELD, 5000L);
        final ShieldPowerUp shield = new ShieldPowerUp(Vector2.ZERO, effect);
        assertSame(effect, shield.getEffect());
        assertEquals("shield", shield.getCollectibleType());
    }

    @Test
    void shieldPowerUpThrowsOnNullEffect() {
        assertThrows(NullPointerException.class, () -> new ShieldPowerUp(Vector2.ZERO, null));
    }

    // Shared AbstractCollectible behaviour

    @Test
    void collectiblesUseTheCollectibleCollisionLayer() {
        final StarCollectible star = new StarCollectible(Vector2.ZERO, 10);
        assertEquals(CollisionLayer.COLLECTIBLE, star.getCollisionLayer());
    }

    @Test
    void collectiblePositionCanBeUpdated() {
        final StarCollectible star = new StarCollectible(Vector2.ZERO, 10);
        final Vector2 newPos = new Vector2(100, 200);
        star.setPosition(newPos);
        assertEquals(newPos, star.getPosition());
    }

    @Test
    void collectibleSetPositionNullThrows() {
        final StarCollectible star = new StarCollectible(Vector2.ZERO, 10);
        assertThrows(NullPointerException.class, () -> star.setPosition(null));
    }
}
