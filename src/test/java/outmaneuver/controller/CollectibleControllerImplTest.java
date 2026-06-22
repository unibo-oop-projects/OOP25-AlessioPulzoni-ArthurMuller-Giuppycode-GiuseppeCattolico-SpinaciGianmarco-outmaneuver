package outmaneuver.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.controller.event.InternalEventListener;
import outmaneuver.controller.impl.CollectibleControllerImpl;
import outmaneuver.model.area.collision.CollisionData;
import outmaneuver.model.area.entity.collectibles.AbstractCollectible;
import outmaneuver.model.area.entity.collectibles.Collectible;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.area.entity.plane.PlaneData;
import outmaneuver.model.area.entity.plane.PlaneImpl;
import outmaneuver.model.session.GameState;
import outmaneuver.model.session.IGameSession;
import outmaneuver.util.Vector2;
import outmaneuver.view.GameView;
import outmaneuver.view.RenderState;

class CollectibleControllerImplTest {

    private static final long SPAWN_INTERVAL_MS = 3000;

    private static class NoOpListener implements InternalEventListener {
        @Override
        public void onInternalEvent(final CollisionEvent evt, final Object data) {
        }
    }

    private static class StubCollectible extends AbstractCollectible {
        boolean applied = false;

        StubCollectible(final Vector2 position) {
            super(position);
        }

        @Override
        public void apply(final Plane plane, final IGameSession session) {
            applied = true;
        }
    }

    private static final IGameSession NO_OP_SESSION = new IGameSession() {
        @Override public GameState getGameState() { return null; }
        @Override public int getScore() { return 0; }
        @Override public long getElapsedTimeMillis() { return 0; }
        @Override public void incrementScore(final int delta) { }
        @Override public void transitionTo(final GameState state) { }
        @Override public void reset() { }
    };

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
    };

    private CollisionEngine collisionEngine;
    private PlaneImpl plane;
    private CollectibleControllerImpl collectibleCtrl;

    @BeforeEach
    void setUp() {
        plane = new PlaneImpl(new PlaneData("standard", 200, 3, 20, "aircraft_standard", 0));
        collisionEngine = new CollisionEngine(new NoOpListener());
        collectibleCtrl = new CollectibleControllerImpl(
                new ArrayList<>(), collisionEngine, NO_OP_SESSION);
        collectibleCtrl.setView(new StubGameView(800, 600));
    }

    // ── spawnEntity / removeEntity (inherited from EntityControllerImpl) ──

    @Test
    void spawnEntity_addsCollectibleToEntities() {
        final StubCollectible col = new StubCollectible(new Vector2(500, 500));
        collectibleCtrl.spawnEntity(col);
        assertTrue(collectibleCtrl.getEntities().contains(col));
    }

    @Test
    void removeEntity_removesCollectible() {
        final StubCollectible col = new StubCollectible(Vector2.ZERO);
        collectibleCtrl.spawnEntity(col);
        collectibleCtrl.removeEntity(col);
        assertFalse(collectibleCtrl.getEntities().contains(col));
    }

    @Test
    void clearAll_removesCollectiblesButKeepsPlane() {
        collectibleCtrl.spawnEntity(plane);
        final StubCollectible col = new StubCollectible(new Vector2(200, 200));
        collectibleCtrl.spawnEntity(col);

        collectibleCtrl.clearAll();

        assertFalse(collectibleCtrl.getEntities().contains(col), "Collectible should be gone after clearAll");
        assertTrue(collectibleCtrl.getEntities().contains(plane), "Plane should stay after clearAll");
    }

    // ── onInternalEvent (inherited from EntityControllerImpl) ─────────────

    @Test
    void collisionEvent_appliesEffectAndRemovesCollectible() {
        final StubCollectible col = new StubCollectible(plane.getPosition());
        collectibleCtrl.spawnEntity(col);

        final CollisionData cd = new CollisionData(plane, col, plane.getPosition());
        collectibleCtrl.onInternalEvent(CollisionEvent.PLANE_COLLECTIBLE_COLLISION, cd);

        assertFalse(collectibleCtrl.getEntities().contains(col), "Collectible should be removed on pickup");
        assertTrue(col.applied, "apply() should have been called");
    }

    @Test
    void unrelatedEvent_collectibleRemains() {
        final StubCollectible col = new StubCollectible(new Vector2(99999, 99999));
        collectibleCtrl.spawnEntity(col);

        collectibleCtrl.onInternalEvent(CollisionEvent.MISSILE_MISSILE_COLLISION, null);

        assertTrue(collectibleCtrl.getEntities().contains(col), "Collectible should remain untouched");
        assertFalse(col.applied, "apply() should not have been called");
    }

    // ── updateEntities – spawn timing (CollectibleControllerImpl-specific) ──

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
                new CollectibleControllerImpl(new ArrayList<>(), collisionEngine, NO_OP_SESSION);
        zeroViewCtrl.setView(new StubGameView(0, 0));
        zeroViewCtrl.spawnEntity(plane);
        zeroViewCtrl.updateEntities(SPAWN_INTERVAL_MS);

        assertEquals(1, zeroViewCtrl.getEntities().size(),
                "Only the plane should remain when the view size is zero");
    }
}
