package outmaneuver.view.swing;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Objects;

import outmaneuver.controller.InputController;
import outmaneuver.controller.MasterController;
import outmaneuver.controller.OutmaneuverEvent;

public final class GameKeyListener extends KeyAdapter {

    private final InputController inputController;
    private final MasterController masterController;

    public GameKeyListener(final InputController inputController,
                           final MasterController masterController) {
        this.inputController = Objects.requireNonNull(inputController, "inputController must not be null");
        this.masterController = Objects.requireNonNull(masterController, "masterController must not be null");
    }

    @Override
    public void keyPressed(final KeyEvent e) {
        inputController.onKeyPressed(e.getKeyCode());
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE -> masterController.handleEvent(OutmaneuverEvent.QUIT_APPLICATION);
            case KeyEvent.VK_P      -> masterController.handleEvent(OutmaneuverEvent.TOGGLE_PAUSE);
            case KeyEvent.VK_G      -> masterController.handleEvent(OutmaneuverEvent.GAME_OVER);
            default -> { }
        }
    }

    @Override
    public void keyReleased(final KeyEvent e) {
        inputController.onKeyReleased(e.getKeyCode());
    }
}
