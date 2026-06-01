package outmaneuver.controller;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.controller.impl.InputControllerImpl;

class InputControllerImplTest {

    private static final int VK_LEFT = 37;
    private static final int VK_RIGHT = 39;
    private static final int VK_A = 65;
    private static final int VK_D = 68;

    private InputControllerImpl input;

    @BeforeEach
    void setUp() {
        input = new InputControllerImpl();
    }

    @Test
    void testDefaultNoTurn() {
        assertEquals(0.0, input.getTurnDirection());
    }

    @Test
    void testLeftArrow() {
        input.onKeyPressed(VK_LEFT);
        assertEquals(-1.0, input.getTurnDirection());
        input.onKeyReleased(VK_LEFT);
        assertEquals(0.0, input.getTurnDirection());
    }

    @Test
    void testRightArrow() {
        input.onKeyPressed(VK_RIGHT);
        assertEquals(1.0, input.getTurnDirection());
        input.onKeyReleased(VK_RIGHT);
        assertEquals(0.0, input.getTurnDirection());
    }

    @Test
    void testAKey() {
        input.onKeyPressed(VK_A);
        assertEquals(-1.0, input.getTurnDirection());
        input.onKeyReleased(VK_A);
        assertEquals(0.0, input.getTurnDirection());
    }

    @Test
    void testDKey() {
        input.onKeyPressed(VK_D);
        assertEquals(1.0, input.getTurnDirection());
        input.onKeyReleased(VK_D);
        assertEquals(0.0, input.getTurnDirection());
    }

    @Test
    void testBothLeftAndRightReturnsZero() {
        input.onKeyPressed(VK_LEFT);
        input.onKeyPressed(VK_RIGHT);
        assertEquals(0.0, input.getTurnDirection());
    }

    @Test
    void testBothAAndDReturnsZero() {
        input.onKeyPressed(VK_A);
        input.onKeyPressed(VK_D);
        assertEquals(0.0, input.getTurnDirection());
    }

    @Test
    void testMixedLeftKeysReturnLeft() {
        input.onKeyPressed(VK_A);
        input.onKeyPressed(VK_LEFT);
        assertEquals(-1.0, input.getTurnDirection());
    }

    @Test
    void testReleaseOneOfTwoLeftKeysStillTurns() {
        input.onKeyPressed(VK_A);
        input.onKeyPressed(VK_LEFT);
        input.onKeyReleased(VK_A);
        assertEquals(-1.0, input.getTurnDirection());
    }

    @Test
    void testIsThrustActiveReturnsFalse() {
        assertFalse(input.isThrustActive());
    }

    @Test
    void testThreadSafety() throws InterruptedException {
        final var thread = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                input.onKeyPressed(VK_LEFT);
                input.onKeyPressed(VK_RIGHT);
            }
        });
        thread.start();
        for (int i = 0; i < 100; i++) {
            input.getTurnDirection();
            input.onKeyReleased(VK_LEFT);
            input.onKeyReleased(VK_RIGHT);
        }
        thread.join();
    }
}
