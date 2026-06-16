package outmaneuver.controller;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.controller.impl.EntityControllerImpl;
import outmaneuver.controller.impl.InputControllerImpl;
import outmaneuver.model.area.Plane;
import outmaneuver.model.area.PlaneImpl;
import outmaneuver.model.area.StandardStats;
import outmaneuver.model.area.TurnState;
import outmaneuver.model.missile.IMissile;
import outmaneuver.util.Vector2;
import outmaneuver.view.MissileRenderData;

class EntityControllerImplTest {

    private static final double EPS = 1e-9;

    // Mock minimale di MissileController per i test
    private static final MissileController DUMMY_MISSILE_CTRL = new MissileController() {
        @Override public void update(outmaneuver.model.area.Plane p, double dt) { }
        @Override public List<MissileRenderData> getRenderData() { return List.of(); }
        @Override public List<IMissile> getActiveMissiles() { return List.of(); }
        @Override public void reset() { }
    };

    private Plane plane;
    private InputControllerImpl input;
    private EntityControllerImpl entityCtrl;

    @BeforeEach
    void setUp() {
        plane = new PlaneImpl(new StandardStats());
        input = new InputControllerImpl();
        entityCtrl = new EntityControllerImpl(plane, input, (evt, data) -> { }, DUMMY_MISSILE_CTRL);
    }

    @Test
    void testNoInputDoesNotChangeDirection() {
        final double initialDir = plane.getDirection();
        entityCtrl.updateEntities(16);
        assertEquals(initialDir, plane.getDirection(), EPS);
    }

    @Test
    void testLeftInputRotatesLeft() {
        input.onKeyPressed(37);
        entityCtrl.updateEntities(100);
        assertTrue(plane.getDirection() < 0, "Direction should be negative (left turn)");
    }

    @Test
    void testRightInputRotatesRight() {
        input.onKeyPressed(39);
        entityCtrl.updateEntities(100);
        assertTrue(plane.getDirection() > 0, "Direction should be positive (right turn)");
    }

    @Test
    void testLeftInputSetsTurnStateLeft() {
        input.onKeyPressed(37);
        entityCtrl.updateEntities(16);
        assertEquals(TurnState.LEFT, plane.getTurnState());
    }

    @Test
    void testRightInputSetsTurnStateRight() {
        input.onKeyPressed(39);
        entityCtrl.updateEntities(16);
        assertEquals(TurnState.RIGHT, plane.getTurnState());
    }

    @Test
    void testNoInputSetsTurnStateNone() {
        entityCtrl.updateEntities(16);
        assertEquals(TurnState.NONE, plane.getTurnState());
    }

    @Test
    void testPositionChangesWhenMoving() {
        final Vector2 initialPos = plane.getPosition();
        entityCtrl.updateEntities(100);
        final Vector2 newPos = plane.getPosition();
        assertNotEquals(initialPos, newPos);
    }

    @Test
    void testPlaneMovesForwardByDefault() {
        plane.setPosition(new Vector2(100, 100));
        plane.setDirection(0);
        entityCtrl.updateEntities(1000);
        final Vector2 pos = plane.getPosition();
        assertTrue(pos.getX() > 100, "Plane should move in +X direction");
        assertEquals(100, pos.getY(), EPS, "Y should stay unchanged");
    }

    @Test
    void testClearAllResetsState() {
        plane.setDirection(Math.PI / 2);
        plane.setPosition(new Vector2(300, 400));
        entityCtrl.clearAll();
        assertEquals(Vector2.ZERO, plane.getPosition());
        assertEquals(0, plane.getDirection(), EPS);
        assertEquals(TurnState.NONE, plane.getTurnState());
    }

    @Test
    void testGetPlaneReturnsSameInstance() {
        assertSame(plane, entityCtrl.getPlane());
    }

    @Test
    void testDeltaCapping() {
        input.onKeyPressed(39);
        entityCtrl.updateEntities(200);
        final double posAfterBigDelta = plane.getPosition().getX();
        assertTrue(posAfterBigDelta > 0);
    }

    @Test
    void testAngleNormalisation() {
        input.onKeyPressed(37);
        for (int i = 0; i < 1000; i++) {
            entityCtrl.updateEntities(100);
        }
        final double dir = plane.getDirection();
        assertTrue(dir >= -Math.PI - EPS && dir <= Math.PI + EPS,
                "Direction should stay within [-PI, PI], was " + dir);
    }
}