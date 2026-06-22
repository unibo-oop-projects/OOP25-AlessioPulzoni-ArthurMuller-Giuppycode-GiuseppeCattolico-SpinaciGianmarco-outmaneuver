package outmaneuver.view.swing;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import outmaneuver.controller.impl.InputControllerImpl;
import outmaneuver.controller.impl.MasterControllerImpl;

class GameKeyListenerTest {

    @Test
    void rejectsNullInputController() {
        final var master = new MasterControllerImpl();
        assertThrows(NullPointerException.class,
                () -> new GameKeyListener(null, master));
    }

    @Test
    void rejectsNullMasterController() {
        assertThrows(NullPointerException.class,
                () -> new GameKeyListener(new InputControllerImpl(), null));
    }
}
