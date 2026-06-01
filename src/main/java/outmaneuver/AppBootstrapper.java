package outmaneuver;

import javax.swing.JFrame;

import outmaneuver.controller.impl.EntityControllerImpl;
import outmaneuver.controller.impl.InputControllerImpl;
import outmaneuver.controller.impl.MasterControllerImpl;
import outmaneuver.model.area.Plane;
import outmaneuver.model.area.PlaneImpl;
import outmaneuver.model.area.StandardStats;
import outmaneuver.model.session.GameState;
import outmaneuver.view.swing.SwingGameView;
import outmaneuver.view.swing.UIManager;
import outmaneuver.view.swing.gameover.GameOverView;
import outmaneuver.view.swing.menu.MainMenuView;

public final class AppBootstrapper {

    private AppBootstrapper() { }

    public static void launch() {
        final JFrame frame = new JFrame("OutManeuver");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        final Plane plane = new PlaneImpl(new StandardStats());
        final InputControllerImpl inputCtrl = new InputControllerImpl();
        final MasterControllerImpl master = new MasterControllerImpl();
        final EntityControllerImpl entity = new EntityControllerImpl(plane, inputCtrl, master);
        master.setEntityController(entity);

        final SwingGameView gameView = new SwingGameView(inputCtrl, master);
        gameView.init();
        master.attachView(gameView);

        final UIManager[] uiManagerRef = { null };

        final GameOverView gameOverView = new GameOverView();
        final MainMenuView mainMenuView = new MainMenuView(
            () -> onStart(uiManagerRef[0], master, gameView),
            () -> System.exit(0)
        );

        final UIManager uiManager = new UIManager(mainMenuView, gameOverView, gameView.getPanel());
        uiManager.showScreen(GameState.MENU);
        uiManagerRef[0] = uiManager;

        frame.add(uiManager);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void onStart(final UIManager uiManager,
                                 final MasterControllerImpl master,
                                 final SwingGameView gameView) {
        uiManager.showScreen(GameState.PLAYING);
        gameView.getPanel().requestFocusInWindow();
        master.start();
    }
}

