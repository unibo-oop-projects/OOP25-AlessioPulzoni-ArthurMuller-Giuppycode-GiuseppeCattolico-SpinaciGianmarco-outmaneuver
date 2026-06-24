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
        final var data = new EntityRenderData(100, 200, 1.5, "aircraft_test", 10);
        assertEquals(100, data.getX());
        assertEquals(200, data.getY());
        assertEquals(1.5, data.getDirectionRad(), EPS);
        assertEquals("aircraft_test", data.getSpriteId());
        assertEquals(10, data.getRadius(), EPS);
    }

    @Test
    void testRenderStateBuilder() {
        final var plane = new PlaneImpl(new PlaneData("standard", 200, 3, 20, "aircraft_standard", 0));
        plane.setPosition(new Vector2(150, 250));
        plane.setDirection(Math.PI / 3);

        final var planeData = new EntityRenderData(
                plane.getPosition().getX(),
                plane.getPosition().getY(),
                plane.getDirection(),
                plane.getStats().getSpriteId(),
                plane.getStats().getHitboxRadius());

        final var state = RenderState.builder()
                .planeData(planeData)
                .build();

        final var result = state.getPlane();
        assertEquals(150, result.getX(), EPS);
        assertEquals(250, result.getY(), EPS);
        assertEquals(Math.PI / 3, result.getDirectionRad(), EPS);
        assertEquals("aircraft_standard", result.getSpriteId());
    }

    @Test
    void testRenderStateImmutability() {
        final var plane = new PlaneImpl(new PlaneData("standard", 200, 3, 20, "aircraft_standard", 0));
        final var planeData = new EntityRenderData(
                plane.getPosition().getX(),
                plane.getPosition().getY(),
                plane.getDirection(),
                plane.getStats().getSpriteId(),
                plane.getStats().getHitboxRadius());

        final var state = RenderState.builder()
                .planeData(planeData)
                .build();

        final var result = state.getPlane();
        assertEquals(0, result.getX(), EPS);

        plane.setPosition(new Vector2(999, 999));
        assertEquals(0, state.getPlane().getX(), EPS);
    }
}
