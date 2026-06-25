package outmaneuver.view.swing;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Objects;

import outmaneuver.controller.InputController;
import outmaneuver.controller.MasterController;
import outmaneuver.controller.event.GameEvent;

/**
 * Forwards key press/release events to the {@link InputController}, and additionally
 * routes the Escape key to the {@link MasterController} to trigger a pause.
 */
public final class GameKeyListener extends KeyAdapter {

    private final InputController inputController;
    private final MasterController masterController;

    /**
     * Creates the key listener.
     *
     * @param inputController receives raw key press/release notifications
     * @param masterController receives the pause event triggered by Escape
     */
    public GameKeyListener(final InputController inputController,
                           final MasterController masterController) {
        this.inputController = Objects.requireNonNull(inputController, "inputController must not be null");
        this.masterController = Objects.requireNonNull(masterController, "masterController must not be null");
    }

    @Override
    public void keyPressed(final KeyEvent e) {
        inputController.onKeyPressed(e.getKeyCode());
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE -> masterController.handleEvent(GameEvent.PAUSED);
            default -> { }
        }
    }

    @Override
    public void keyReleased(final KeyEvent e) {
        inputController.onKeyReleased(e.getKeyCode());
    }
}
