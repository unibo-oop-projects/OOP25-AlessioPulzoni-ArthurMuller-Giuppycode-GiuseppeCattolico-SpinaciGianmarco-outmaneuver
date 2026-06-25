package outmaneuver.model.area;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.model.area.entity.plane.PlaneData;
import outmaneuver.model.area.entity.plane.PlaneImpl;
import outmaneuver.model.area.entity.plane.PlaneStats;
import outmaneuver.model.area.entity.plane.TurnState;
import outmaneuver.util.Vector2;

class PlaneImplTest {

    private static final double EPS = 1e-12;

    private PlaneImpl plane;

    @BeforeEach
    void setUp() {
        plane = new PlaneImpl(new PlaneData("standard", 200, 3, 20, "aircraft_standard", 0));
    }

    @Test
    void testDefaultConstruction() {
        assertEquals(Vector2.ZERO, plane.getPosition());
        assertEquals(0.0, plane.getDirection(), EPS);
        assertEquals(TurnState.NONE, plane.getTurnState());
        assertEquals("standard", plane.getStats().getId());
    }

    @Test
    void testSetPosition() {
        final var pos = new Vector2(100, 200);
        plane.setPosition(pos);
        assertEquals(pos, plane.getPosition());
    }

    @Test
    void testSetPositionNullThrows() {
        assertThrows(NullPointerException.class, () -> plane.setPosition(null));
    }

    @Test
    void testSetDirection() {
        plane.setDirection(Math.PI / 4);
        assertEquals(Math.PI / 4, plane.getDirection(), EPS);
    }

    @Test
    void testSetStats() {
        final var customStats = new PlaneStats() {
            @Override public String getId() { return "custom"; }
            @Override public double getBaseSpeed() { return 999; }
            @Override public double getTurnRate() { return 5; }
            @Override public double getHitboxRadius() { return 10; }
            @Override public String getSpriteId() { return "custom_sprite"; }
        };
        plane.setStats(customStats);
        assertEquals("custom", plane.getStats().getId());
        assertEquals(999, plane.getStats().getBaseSpeed());
    }

    @Test
    void testSetStatsNullThrows() {
        assertThrows(NullPointerException.class, () -> plane.setStats(null));
    }

    @Test
    void testTurnState() {
        assertEquals(TurnState.NONE, plane.getTurnState());
        plane.setTurnState(TurnState.LEFT);
        assertEquals(TurnState.LEFT, plane.getTurnState());
        plane.setTurnState(TurnState.RIGHT);
        assertEquals(TurnState.RIGHT, plane.getTurnState());
        plane.setTurnState(TurnState.NONE);
        assertEquals(TurnState.NONE, plane.getTurnState());
    }

    @Test
    void testSetTurnStateNullThrows() {
        assertThrows(NullPointerException.class, () -> plane.setTurnState(null));
    }

    @Test
    void testPlaneData() {
        final var stats = new PlaneData("interceptor", 280, 2, 15, "aircraft_interceptor", 500);
        assertEquals("interceptor", stats.getId());
        assertEquals(280.0, stats.getBaseSpeed());
        assertEquals(2.0, stats.getTurnRate());
        assertEquals(15.0, stats.getHitboxRadius());
        assertEquals("aircraft_interceptor", stats.getSpriteId());
        assertEquals(500, stats.price());
    }

    @Test
    void testTurnStateEnumValues() {
        assertEquals(3, TurnState.values().length);
        assertNotNull(TurnState.valueOf("NONE"));
        assertNotNull(TurnState.valueOf("LEFT"));
        assertNotNull(TurnState.valueOf("RIGHT"));
    }
}
