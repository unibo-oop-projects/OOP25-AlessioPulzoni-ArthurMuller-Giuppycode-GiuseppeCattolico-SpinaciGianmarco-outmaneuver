package outmaneuver.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class Vector2Test {

    private static final double EPS = 1e-12;

    @Test
    void testConstruction() {
        final var v = new Vector2(3.0, 4.0);
        assertEquals(3.0, v.getX());
        assertEquals(4.0, v.getY());
    }

    @Test
    void testZeroConstant() {
        assertEquals(0.0, Vector2.ZERO.getX());
        assertEquals(0.0, Vector2.ZERO.getY());
    }

    @Test
    void testAdd() {
        final var a = new Vector2(1, 2);
        final var b = new Vector2(3, 4);
        final var sum = a.add(b);
        assertEquals(new Vector2(4, 6), sum);
    }

    @Test
    void testAddIsImmutable() {
        final var a = new Vector2(1, 2);
        a.add(new Vector2(3, 4));
        assertEquals(new Vector2(1, 2), a);
    }

    @Test
    void testScale() {
        final var v = new Vector2(2, 3);
        assertEquals(new Vector2(4, 6), v.scale(2));
        assertEquals(new Vector2(-1, -1.5), v.scale(-0.5));
    }

    @Test
    void testScaleIsImmutable() {
        final var v = new Vector2(2, 3);
        v.scale(2);
        assertEquals(new Vector2(2, 3), v);
    }

    @Test
    void testMagnitude() {
        assertEquals(5.0, new Vector2(3, 4).magnitude(), EPS);
        assertEquals(0.0, Vector2.ZERO.magnitude(), EPS);
        assertEquals(1.0, new Vector2(1, 0).magnitude(), EPS);
    }

    @Test
    void testNormalize() {
        final var v = new Vector2(3, 4);
        final var n = v.normalize();
        assertEquals(1.0, n.magnitude(), EPS);
        assertEquals(3.0 / 5.0, n.getX(), EPS);
        assertEquals(4.0 / 5.0, n.getY(), EPS);
    }

    @Test
    void testNormalizeZero() {
        assertEquals(Vector2.ZERO, Vector2.ZERO.normalize());
    }

    @Test
    void testNormalizeIsImmutable() {
        final var v = new Vector2(3, 4);
        v.normalize();
        assertEquals(new Vector2(3, 4), v);
    }

    @Test
    void testDot() {
        final var a = new Vector2(1, 0);
        final var b = new Vector2(0, 1);
        assertEquals(0.0, a.dot(b), EPS);
        assertEquals(1.0, a.dot(new Vector2(1, 0)), EPS);
        assertEquals(25.0, new Vector2(3, 4).dot(new Vector2(3, 4)), EPS);
    }

    @Test
    void testFromAngleRight() {
        final var v = Vector2.fromAngle(0);
        assertEquals(1.0, v.getX(), EPS);
        assertEquals(0.0, v.getY(), EPS);
    }

    @Test
    void testFromAngleUp() {
        final var v = Vector2.fromAngle(-Math.PI / 2);
        assertEquals(0.0, v.getX(), EPS);
        assertEquals(-1.0, v.getY(), EPS);
    }

    @Test
    void testFromAngleDown() {
        final var v = Vector2.fromAngle(Math.PI / 2);
        assertEquals(0.0, v.getX(), EPS);
        assertEquals(1.0, v.getY(), EPS);
    }

    @Test
    void testFromAngleLeft() {
        final var v = Vector2.fromAngle(Math.PI);
        assertEquals(-1.0, v.getX(), EPS);
        assertEquals(0.0, v.getY(), EPS);
    }

    @Test
    void testFromAngleUnitMagnitude() {
        for (double a = 0; a < 2 * Math.PI; a += 0.1) {
            final var v = Vector2.fromAngle(a);
            assertEquals(1.0, v.magnitude(), EPS);
        }
    }

    @Test
    void testAngle() {
        assertEquals(0.0, new Vector2(1, 0).angle(), EPS);
        assertEquals(Math.PI / 2, new Vector2(0, 1).angle(), EPS);
        assertEquals(Math.PI, new Vector2(-1, 0).angle(), EPS);
        assertEquals(-Math.PI / 2, new Vector2(0, -1).angle(), EPS);
    }

    @Test
    void testEqualsAndHashCode() {
        final var a = new Vector2(1.5, 2.5);
        final var b = new Vector2(1.5, 2.5);
        final var c = new Vector2(1.5, 2.6);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(null, a);
        assertNotEquals("not a vector", a);
        final var same = a;
        assertEquals(a, same);
    }

    @Test
    void testToString() {
        final var v = new Vector2(1.5, 2.5);
        assertTrue(v.toString().contains("1.5"));
        assertTrue(v.toString().contains("2.5"));
    }
}
