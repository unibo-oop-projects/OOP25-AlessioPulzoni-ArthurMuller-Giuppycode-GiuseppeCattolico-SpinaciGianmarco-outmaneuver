package outmaneuver.view;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import outmaneuver.model.area.entity.plane.PlaneData;
import outmaneuver.model.area.entity.plane.PlaneImpl;
import outmaneuver.util.Vector2;

class DtoTest {

    private static final double EPS = 1e-12;

    @Test
    void testEntityRenderData() {
        final var data = new EntityRenderData(100, 200, 1.5, "aircraft_test");
        assertEquals(100, data.getX());
        assertEquals(200, data.getY());
        assertEquals(1.5, data.getDirectionRad(), EPS);
        assertEquals("aircraft_test", data.getSpriteId());
    }

    @Test
    void testBankStateValues() {
        assertEquals(3, BankState.values().length);
        assertNotNull(BankState.valueOf("NONE"));
        assertNotNull(BankState.valueOf("LEFT"));
        assertNotNull(BankState.valueOf("RIGHT"));
    }

    @Test
    void testRenderStateBuilder() {
        final var plane = new PlaneImpl(new PlaneData("standard", 200, 3, 20, "aircraft_standard", 0));
        plane.setPosition(new Vector2(150, 250));
        plane.setDirection(Math.PI / 3);

        final var state = RenderState.builder()
                .plane(plane)
                .build();

        final var planeData = state.getPlane();
        assertEquals(150, planeData.getX(), EPS);
        assertEquals(250, planeData.getY(), EPS);
        assertEquals(Math.PI / 3, planeData.getDirectionRad(), EPS);
        assertEquals("aircraft_standard", planeData.getSpriteId());
    }

    @Test
    void testRenderStateImmutability() {
        final var plane = new PlaneImpl(new PlaneData("standard", 200, 3, 20, "aircraft_standard", 0));
        final var state = RenderState.builder()
                .plane(plane)
                .build();

        final var planeData = state.getPlane();
        assertEquals(0, planeData.getX(), EPS);

        plane.setPosition(new Vector2(999, 999));
        assertEquals(0, state.getPlane().getX(), EPS);
    }
}
