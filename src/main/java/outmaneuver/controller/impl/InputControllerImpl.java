package outmaneuver.controller.impl;

import outmaneuver.controller.InputController;
import java.util.HashSet;
import java.util.Set;

/**
 * Default {@link InputController} implementation that tracks pressed keys and derives
 * a turn direction from the left/right key bindings (arrow keys and A/D). All methods
 * are synchronized so input events from the UI thread are safely visible to the game
 * loop thread.
 */
public final class InputControllerImpl implements InputController {

    private static final int KEY_LEFT_A = 65;
    private static final int KEY_LEFT_ARROW = 37;
    private static final int KEY_RIGHT_D = 68;
    private static final int KEY_RIGHT_ARROW = 39;

    private final Set<Integer> pressedKeys = new HashSet<>();

    @Override
    public synchronized void onKeyPressed(final int keyCode) {
        pressedKeys.add(keyCode);
    }

    @Override
    public synchronized void onKeyReleased(final int keyCode) {
        pressedKeys.remove(keyCode);
    }

    @Override
    public synchronized void reset() {
        pressedKeys.clear();
    }

    @Override
    public synchronized double getTurnDirection() {
        final boolean left = pressedKeys.contains(KEY_LEFT_A)
                || pressedKeys.contains(KEY_LEFT_ARROW);
        final boolean right = pressedKeys.contains(KEY_RIGHT_D)
                || pressedKeys.contains(KEY_RIGHT_ARROW);
        if (left == right) {
            return 0.0;
        }
        return left ? -1.0 : 1.0;
    }

}
