package outmaneuver.controller.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.controller.CollisionEngine;
import outmaneuver.controller.ScoreController;
import outmaneuver.controller.impl.CollectibleControllerImpl;
import outmaneuver.controller.impl.InputControllerImpl;
import outmaneuver.controller.impl.MasterControllerImpl;
import outmaneuver.controller.impl.PlaneControllerImpl;
import outmaneuver.controller.impl.ScoreControllerImpl;
import outmaneuver.controller.impl.SessionState;
import outmaneuver.controller.impl.missile.MissileControllerImpl;
import outmaneuver.controller.impl.missile.MissileSpawnDirector;
import outmaneuver.model.area.collision.CollisionData;
import outmaneuver.model.area.effect.EffectImpl;
import outmaneuver.model.area.effect.EffectType;
import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.collectibles.ShieldPowerUp;
import outmaneuver.model.area.entity.collectibles.SpeedBoost;
import outmaneuver.model.area.entity.collectibles.StarCollectible;
import outmaneuver.model.area.entity.missile.MissileImpl;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.model.area.entity.missile.data.MissileRepository;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.area.entity.plane.PlaneData;
import outmaneuver.model.area.entity.plane.PlaneImpl;
import outmaneuver.model.session.ScoreSession;
import outmaneuver.util.Vector2;

/**
 * EventController owns the actual reaction to collisions and effects (removing
 * entities, scoring, shield/speed-boost bookkeeping) — responsibilities that used
 * to live directly in the entity controllers. This wires up the same real
 * collaborators {@link outmaneuver.factory.ControllerAssembler} uses (one shared
 * entity list, one CollisionEngine, the three EntityControllers) so the routing
 * logic is exercised end-to-end without mocking it away.
 */
class EventControllerTest {

    /** Missile concreto minimale per i test: nessun comportamento di update necessario. */
    private static final class TestMissile extends MissileImpl {
        TestMissile(final Vector2 pos) {
            super(pos, new MissileData("test", 1.0, 0.0, 8.0, -1.0, 0.0, 0, null));
        }
    }

    private static final MissileRepository EMPTY_MISSILE_REPO = type -> Optional.empty();

    private MasterControllerImpl master;
    private SessionState session;
    private List<Entity> sharedEntities;
    private Plane plane;
    private PlaneControllerImpl planeCtrl;
    private CollectibleControllerImpl collectibleCtrl;
    private MissileControllerImpl missileCtrl;
    private ScoreSession scoreSession;
    private ScoreController scoreController;
    private AtomicBoolean gameOverTriggered;
    private EventController eventController;

    @BeforeEach
    void setUp() {
        sharedEntities = new ArrayList<>();
        final CollisionEngine collisionEngine = new CollisionEngine((evt, data) -> { });

        final InputControllerImpl input = new InputControllerImpl();
        planeCtrl = new PlaneControllerImpl(input, sharedEntities, collisionEngine);
        collectibleCtrl = new CollectibleControllerImpl(sharedEntities, collisionEngine);
        missileCtrl = new MissileControllerImpl(sharedEntities, collisionEngine, EMPTY_MISSILE_REPO, new MissileSpawnDirector());

        plane = new PlaneImpl(new PlaneData("standard", 200, 3, 20, "plane_standard", 0));
        planeCtrl.spawnEntity(plane);

        master = new MasterControllerImpl();
        master.addEntityController(planeCtrl);
        master.addEntityController(collectibleCtrl);
        master.addEntityController(missileCtrl);

        session = new SessionState();
        scoreSession = new ScoreSession();
        master.setSessionState(session);
        scoreController = new ScoreControllerImpl(scoreSession);
        gameOverTriggered = new AtomicBoolean(false);

        eventController = new EventController(master, session, scoreController, () -> gameOverTriggered.set(true));

        // Mirrors ControllerAssembler: the EventController is each EntityController's
        // internal-event sink, so effects raised by CollectibleControllerImpl route back here.
        planeCtrl.setEventListener(eventController);
        collectibleCtrl.setEventListener(eventController);
        missileCtrl.setEventListener(eventController);
    }

    // ── PLANE_MISSILE_COLLISION ────────────────────────────────────────

    @Test
    void planeMissileCollision_removesMissileAndTriggersGameOverWithoutShield() {
        final MissileImpl missile = new TestMissile(plane.getPosition());
        missileCtrl.spawnEntity(missile);

        final CollisionData data = new CollisionData(missile, plane, plane.getPosition());
        eventController.onInternalEvent(CollisionEvent.PLANE_MISSILE_COLLISION, data);

        assertFalse(sharedEntities.contains(missile), "Missile should be removed on plane hit");
        assertTrue(gameOverTriggered.get(), "Without an active shield, a plane hit should trigger game over");
    }

    @Test
    void planeMissileCollision_withActiveShieldRemovesMissileButDoesNotTriggerGameOver() {
        eventController.onInternalEvent(EffectEvent.EFFECT_APPLIED, new EffectImpl(EffectType.SHIELD, 5000L));

        final MissileImpl missile = new TestMissile(plane.getPosition());
        missileCtrl.spawnEntity(missile);
        final CollisionData data = new CollisionData(missile, plane, plane.getPosition());
        eventController.onInternalEvent(CollisionEvent.PLANE_MISSILE_COLLISION, data);

        assertFalse(sharedEntities.contains(missile), "Missile should still be removed on plane hit");
        assertFalse(gameOverTriggered.get(), "An active shield should prevent game over");
    }

    @Test
    void onInternalEvent_collisionWithoutCollisionDataIsIgnored() {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> eventController.onInternalEvent(CollisionEvent.PLANE_MISSILE_COLLISION, null));
    }

    // ── PLANE_COLLECTIBLE_COLLISION ────────────────────────────────────

    @Test
    void planeCollectibleCollision_starCollectible_removesItAndAwardsItsScoreValue() {
        final StarCollectible star = new StarCollectible(plane.getPosition(), 15);
        collectibleCtrl.spawnEntity(star);

        final CollisionData data = new CollisionData(plane, star, plane.getPosition());
        eventController.onInternalEvent(CollisionEvent.PLANE_COLLECTIBLE_COLLISION, data);

        assertFalse(sharedEntities.contains(star), "Collected star should be removed");
        assertEquals(15, scoreSession.getScore());
        assertFalse(collectibleCtrl.hasEffect(EffectImpl.class), "A star carries no effect to activate");
    }

    @Test
    void planeCollectibleCollision_speedBoost_activatesEffectAndAppliesMultiplier() {
        final SpeedBoost boost = new SpeedBoost(plane.getPosition(), new EffectImpl(EffectType.SPEED_BOOST, 2.0, 3000L));
        collectibleCtrl.spawnEntity(boost);

        final CollisionData data = new CollisionData(plane, boost, plane.getPosition());
        eventController.onInternalEvent(CollisionEvent.PLANE_COLLECTIBLE_COLLISION, data);

        assertFalse(sharedEntities.contains(boost), "Collected speed boost should be removed");
        assertTrue(collectibleCtrl.hasEffect(EffectImpl.class));
        assertEquals(2.0, collectibleCtrl.getEffectMultiplier());
        assertEquals(2.0, session.getSpeedMultiplier());
    }

    @Test
    void planeCollectibleCollision_shieldPowerUp_activatesShield() {
        final ShieldPowerUp shield = new ShieldPowerUp(plane.getPosition(), new EffectImpl(EffectType.SHIELD, 5000L));
        collectibleCtrl.spawnEntity(shield);

        final CollisionData data = new CollisionData(plane, shield, plane.getPosition());
        eventController.onInternalEvent(CollisionEvent.PLANE_COLLECTIBLE_COLLISION, data);

        assertTrue(session.isShieldActive());
    }

    // ── MISSILE_MISSILE_COLLISION ──────────────────────────────────────

    @Test
    void missileMissileCollision_removesBothMissilesAndAwardsTwentyPoints() {
        final MissileImpl a = new TestMissile(new Vector2(0, 0));
        final MissileImpl b = new TestMissile(new Vector2(5, 0));
        missileCtrl.spawnEntity(a);
        missileCtrl.spawnEntity(b);

        final CollisionData data = new CollisionData(a, b, Vector2.ZERO);
        eventController.onInternalEvent(CollisionEvent.MISSILE_MISSILE_COLLISION, data);

        assertFalse(sharedEntities.contains(a), "First missile should be removed");
        assertFalse(sharedEntities.contains(b), "Second missile should be removed");
        assertEquals(20, scoreSession.getScore());
    }

    // ── EFFECT_EXPIRED ──────────────────────────────────────────────────

    @Test
    void shieldEffectExpired_clearsShield() {
        eventController.onInternalEvent(EffectEvent.EFFECT_APPLIED, new EffectImpl(EffectType.SHIELD, 5000L));
        assertTrue(session.isShieldActive());

        eventController.onInternalEvent(EffectEvent.EFFECT_EXPIRED, new EffectImpl(EffectType.SHIELD, 0L));
        assertFalse(session.isShieldActive());
    }

    @Test
    void speedBoostEffectExpired_resetsMultiplierToOne() {
        eventController.onInternalEvent(EffectEvent.EFFECT_APPLIED, new EffectImpl(EffectType.SPEED_BOOST, 3.0, 3000L));
        assertEquals(3.0, session.getSpeedMultiplier());

        eventController.onInternalEvent(EffectEvent.EFFECT_EXPIRED, new EffectImpl(EffectType.SPEED_BOOST, 1.0, 0L));
        assertEquals(1.0, session.getSpeedMultiplier());
    }

    @Test
    void shieldEffectExpired_afterPlaneMissileCollisionAllowsGameOverAgain() {
        eventController.onInternalEvent(EffectEvent.EFFECT_APPLIED, new EffectImpl(EffectType.SHIELD, 5000L));
        eventController.onInternalEvent(EffectEvent.EFFECT_EXPIRED, new EffectImpl(EffectType.SHIELD, 0L));

        final MissileImpl missile = new TestMissile(plane.getPosition());
        missileCtrl.spawnEntity(missile);
        eventController.onInternalEvent(CollisionEvent.PLANE_MISSILE_COLLISION,
                new CollisionData(missile, plane, plane.getPosition()));

        assertTrue(gameOverTriggered.get(), "Once the shield has expired, a plane hit should trigger game over again");
    }
}
