package outmaneuver.model.area.effect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EffectImplTest {

    @Test
    void isActiveImmediatelyAfterCreation() {
        final Effect effect = new EffectImpl(EffectType.SHIELD, 1000L);
        assertTrue(effect.isActive());
    }

    @Test
    void becomesInactiveOnceRemainingDurationIsExhausted() {
        final Effect effect = new EffectImpl(EffectType.SHIELD, 100L);
        effect.update(100L);
        assertFalse(effect.isActive());
    }

    @Test
    void remainsActiveBeforeDurationElapses() {
        final Effect effect = new EffectImpl(EffectType.SHIELD, 100L);
        effect.update(50L);
        assertTrue(effect.isActive());
    }

    @Test
    void durationIsConsumedAcrossMultipleUpdates() {
        final Effect effect = new EffectImpl(EffectType.SHIELD, 100L);
        effect.update(40L);
        assertTrue(effect.isActive());
        effect.update(40L);
        assertTrue(effect.isActive());
        effect.update(40L);
        assertFalse(effect.isActive());
    }

    @Test
    void typeReturnsConfiguredType() {
        assertEquals(EffectType.SHIELD, new EffectImpl(EffectType.SHIELD, 1000L).getType());
        assertEquals(EffectType.SPEED_BOOST, new EffectImpl(EffectType.SPEED_BOOST, 2.0, 1000L).getType());
    }

    @Test
    void multiplierDefaultsToZeroWithoutDurationOnlyConstructor() {
        assertEquals(0.0, new EffectImpl(EffectType.SHIELD, 1000L).getMultiplier());
    }

    @Test
    void multiplierReturnsConfiguredValue() {
        assertEquals(2.5, new EffectImpl(EffectType.SPEED_BOOST, 2.5, 1000L).getMultiplier());
    }
}
