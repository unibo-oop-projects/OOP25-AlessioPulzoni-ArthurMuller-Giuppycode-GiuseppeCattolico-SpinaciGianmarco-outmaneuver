package outmaneuver.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.controller.event.EffectEvent;
import outmaneuver.controller.event.Event;
import outmaneuver.controller.event.InternalEventListener;
import outmaneuver.controller.impl.CollectibleControllerImpl;
import outmaneuver.model.area.effect.Effect;
import outmaneuver.model.area.effect.EffectImpl;
import outmaneuver.model.area.effect.EffectType;
import outmaneuver.model.area.entity.collectibles.Collectible;
import outmaneuver.model.area.entity.collectibles.StarCollectible;
import outmaneuver.model.area.entity.plane.PlaneData;
import outmaneuver.model.area.entity.plane.PlaneImpl;
import outmaneuver.util.Vector2;
import outmaneuver.view.GameView;
import outmaneuver.view.RenderState;

class CollectibleControllerImplTest {

    private static final long SPAWN_INTERVAL_MS = 3000;

    private static class RecordingListener implements InternalEventListener {
        final List<Event> events = new ArrayList<>();
        final List<Object> payloads = new ArrayList<>();

        @Override
        public void onInternalEvent(final Event evt, final Object data) {
            events.add(evt);
            payloads.add(data);
        }
    }

    private static final class StubGameView implements GameView {
        private final int width;
        private final int height;

        StubGameView(final int width, final int height) {
            this.width = width;
            this.height = height;
        }

        @Override public void renderFrame(final RenderState state) { }
        @Override public int getWidth() { return width; }
        @Override public int getHeight() { return height; }
    }

    private CollisionEngine collisionEngine;
    private PlaneImpl plane;
    private RecordingListener listener;
    private CollectibleControllerImpl collectibleCtrl;

    @BeforeEach
    void setUp() {
        plane = new PlaneImpl(new PlaneData("standard", 200, 3, 20, "aircraft_standard", 0));
        listener = new RecordingListener();
        collisionEngine = new CollisionEngine(listener);
        collectibleCtrl = new CollectibleControllerImpl(new ArrayList<>(), collisionEngine);
        collectibleCtrl.setEventListener(listener);
        collectibleCtrl.setView(new StubGameView(800, 600));
    }

    // ── spawnEntity / removeEntity (inherited from EntityControllerImpl) ──

    @Test
    void spawnEntity_addsCollectibleToEntities() {
        final Collectible col = star(new Vector2(500, 500));
        collectibleCtrl.spawnEntity(col);
        assertTrue(collectibleCtrl.getEntities().contains(col));
    }

    @Test
    void removeEntity_removesCollectible() {
        final Collectible col = star(Vector2.ZERO);
        collectibleCtrl.spawnEntity(col);
        collectibleCtrl.removeEntity(col);
        assertFalse(collectibleCtrl.getEntities().contains(col));
    }

    // ── addEffect / hasEffect / getEffectMultiplier ───────────────────

    @Test
    void addEffect_activatesEffectAndFiresEffectApplied() {
        final Effect effect = new EffectImpl(EffectType.SPEED_BOOST, 2.0, 3000L);
        collectibleCtrl.addEffect(effect);

        assertTrue(collectibleCtrl.hasEffect(EffectImpl.class));
        assertEquals(2.0, collectibleCtrl.getEffectMultiplier());
        assertTrue(listener.events.contains(EffectEvent.EFFECT_APPLIED));
        assertTrue(listener.payloads.contains(effect));
    }

    @Test
    void addEffect_sameTypeReplacesPreviousEffect() {
        collectibleCtrl.addEffect(new EffectImpl(EffectType.SPEED_BOOST, 2.0, 3000L));
        final Effect replacement = new EffectImpl(EffectType.SPEED_BOOST, 4.0, 1000L);
        collectibleCtrl.addEffect(replacement);

        assertEquals(4.0, collectibleCtrl.getEffectMultiplier(),
                "A new effect of the same type should replace the active one rather than stack");
    }

    @Test
    void hasEffect_falseWhenNoEffectActive() {
        assertFalse(collectibleCtrl.hasEffect(EffectImpl.class));
    }

    @Test
    void getEffectMultiplier_defaultsToOneWithoutActiveEffect() {
        assertEquals(1.0, collectibleCtrl.getEffectMultiplier());
    }

    // ── updateEntities – effect expiry (CollectibleControllerImpl-specific) ──

    @Test
    void updateEntities_expiresEffectAfterItsDurationAndFiresEffectExpired() {
        final Effect effect = new EffectImpl(EffectType.SHIELD, 100L);
        collectibleCtrl.addEffect(effect);
        listener.events.clear();
        listener.payloads.clear();

        collectibleCtrl.updateEntities(150L);

        assertFalse(collectibleCtrl.hasEffect(EffectImpl.class), "Expired effect should no longer be active");
        assertTrue(listener.events.contains(EffectEvent.EFFECT_EXPIRED));
        assertTrue(listener.payloads.contains(effect));
    }

    @Test
    void updateEntities_doesNotExpireEffectBeforeItsDuration() {
        collectibleCtrl.addEffect(new EffectImpl(EffectType.SHIELD, 1000L));
        collectibleCtrl.updateEntities(10L);
        assertTrue(collectibleCtrl.hasEffect(EffectImpl.class));
    }

    // ── clearAll – clears active effects only, leaves entities untouched ──

    @Test
    void clearAll_clearsActiveEffectsAndFiresEffectExpired() {
        final Effect effect = new EffectImpl(EffectType.SHIELD, 5000L);
        collectibleCtrl.addEffect(effect);
        listener.events.clear();
        listener.payloads.clear();

        collectibleCtrl.clearAll();

        assertFalse(collectibleCtrl.hasEffect(EffectImpl.class));
        assertTrue(listener.events.contains(EffectEvent.EFFECT_EXPIRED));
    }

    @Test
    void clearAll_doesNotRemoveSpawnedEntities() {
        collectibleCtrl.spawnEntity(plane);
        final Collectible col = star(new Vector2(200, 200));
        collectibleCtrl.spawnEntity(col);

        collectibleCtrl.clearAll();

        assertTrue(collectibleCtrl.getEntities().contains(plane));
        assertTrue(collectibleCtrl.getEntities().contains(col),
                "clearAll only resets active effects; entity cleanup is handled elsewhere");
    }

    // ── updateEntities – spawn timing ─────────────────────────────────

    @Test
    void updateEntities_doesNotSpawnBeforeInterval() {
        collectibleCtrl.spawnEntity(plane);
        collectibleCtrl.updateEntities(SPAWN_INTERVAL_MS - 1);
        assertEquals(1, collectibleCtrl.getEntities().size(),
                "Only the plane should be present before the spawn interval elapses");
    }

    @Test
    void updateEntities_spawnsCollectibleAfterInterval() {
        collectibleCtrl.spawnEntity(plane);
        collectibleCtrl.updateEntities(SPAWN_INTERVAL_MS);

        final long collectibleCount = collectibleCtrl.getEntities().stream()
                .filter(e -> e instanceof Collectible)
                .count();
        assertEquals(1, collectibleCount, "A collectible should spawn once the interval elapses");
    }

    @Test
    void updateEntities_accumulatesDeltaAcrossTicks() {
        collectibleCtrl.spawnEntity(plane);
        collectibleCtrl.updateEntities(SPAWN_INTERVAL_MS / 2);
        collectibleCtrl.updateEntities(SPAWN_INTERVAL_MS / 2);

        final long collectibleCount = collectibleCtrl.getEntities().stream()
                .filter(e -> e instanceof Collectible)
                .count();
        assertEquals(1, collectibleCount, "Accumulated deltas should trigger a spawn once their sum reaches the interval");
    }

    @Test
    void updateEntities_doesNotSpawnWithoutPlane() {
        collectibleCtrl.updateEntities(SPAWN_INTERVAL_MS);
        assertTrue(collectibleCtrl.getEntities().isEmpty(),
                "No spawn should happen without a plane to anchor the spawn position");
    }

    @Test
    void updateEntities_doesNotSpawnWithZeroViewSize() {
        final CollectibleControllerImpl zeroViewCtrl =
                new CollectibleControllerImpl(new ArrayList<>(), collisionEngine);
        zeroViewCtrl.setView(new StubGameView(0, 0));
        zeroViewCtrl.spawnEntity(plane);
        zeroViewCtrl.updateEntities(SPAWN_INTERVAL_MS);

        assertEquals(1, zeroViewCtrl.getEntities().size(),
                "Only the plane should remain when the view size is zero");
    }

    private static Collectible star(final Vector2 position) {
        return new StarCollectible(position, 10);
    }
}
