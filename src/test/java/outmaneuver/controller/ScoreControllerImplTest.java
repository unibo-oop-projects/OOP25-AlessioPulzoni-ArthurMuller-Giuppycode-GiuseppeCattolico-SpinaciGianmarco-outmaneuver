package outmaneuver.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.controller.event.CollisionEvent;
import outmaneuver.controller.impl.ScoreControllerImpl;
import outmaneuver.model.area.collision.CollisionData;
import outmaneuver.model.area.entity.collectibles.StarCollectible;
import outmaneuver.model.session.ISession;
import outmaneuver.model.session.Session;
import outmaneuver.util.Vector2;

class ScoreControllerImplTest {

    private ISession session;
    private ScoreControllerImpl scoreCtrl;

    @BeforeEach
    void setUp() {
        session = new Session();
        scoreCtrl = new ScoreControllerImpl(session);
    }

    @Test
    void constructorRejectsNullSession() {
        assertThrows(NullPointerException.class, () -> new ScoreControllerImpl(null));
    }

    @Test
    void onTickAwardsOnePointPerElapsedSecond() {
        scoreCtrl.onTick(1000);
        assertEquals(1, session.getScore());
    }

    @Test
    void onTickAccumulatesPartialMillisecondsAcrossTicks() {
        scoreCtrl.onTick(600);
        assertEquals(0, session.getScore(), "Less than a second elapsed: no point yet");
        scoreCtrl.onTick(600);
        assertEquals(1, session.getScore(), "The two partial ticks sum to over a second");
    }

    @Test
    void onTickAwardsMultiplePointsForMultipleElapsedSeconds() {
        scoreCtrl.onTick(3500);
        assertEquals(3, session.getScore());
    }

    @Test
    void resetClearsPendingAccumulatedTime() {
        scoreCtrl.onTick(900);
        scoreCtrl.reset();
        scoreCtrl.onTick(900);
        assertEquals(0, session.getScore(), "reset() should drop the 900ms accumulated before it");
    }

    @Test
    void onInternalEventStarCollectibleAddsItsScoreValue() {
        final StarCollectible star = new StarCollectible(Vector2.ZERO, 25);
        scoreCtrl.onInternalEvent(CollisionEvent.PLANE_COLLECTIBLE_COLLISION, star);
        assertEquals(25, session.getScore());
    }

    @Test
    void onInternalEventMissileMissileCollisionAddsTwentyPoints() {
        scoreCtrl.onInternalEvent(CollisionEvent.MISSILE_MISSILE_COLLISION,
                new CollisionData(null, null, Vector2.ZERO));
        assertEquals(20, session.getScore());
    }

    @Test
    void onInternalEventPlaneMissileCollisionDoesNotAwardPoints() {
        scoreCtrl.onInternalEvent(CollisionEvent.PLANE_MISSILE_COLLISION,
                new CollisionData(null, null, Vector2.ZERO));
        assertEquals(0, session.getScore());
    }
}
