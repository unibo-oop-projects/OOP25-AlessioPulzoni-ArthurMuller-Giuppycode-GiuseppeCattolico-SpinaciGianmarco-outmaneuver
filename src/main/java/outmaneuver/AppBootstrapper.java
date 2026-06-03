package outmaneuver;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

import outmaneuver.controller.MasterController;
import outmaneuver.controller.impl.EntityControllerImpl;
import outmaneuver.controller.impl.HudControllerImpl;
import outmaneuver.controller.impl.InputControllerImpl;
import outmaneuver.controller.impl.MasterControllerImpl;
import outmaneuver.model.area.Plane;
import outmaneuver.model.area.PlaneImpl;
import outmaneuver.model.area.StandardStats;
import outmaneuver.model.leaderboard.JsonLeaderboardRepository;
import outmaneuver.model.leaderboard.Leaderboard;
import outmaneuver.model.session.GameState;
import outmaneuver.view.swing.GameKeyListener;
import outmaneuver.view.swing.SwingGameView;
import outmaneuver.view.swing.UIManager;
import outmaneuver.view.swing.gameover.GameOverView;
import outmaneuver.view.swing.hud.SwingHudView;
import outmaneuver.view.swing.menu.MainMenuView;

public final class AppBootstrapper {

    private AppBootstrapper() { }

    public static void launch() {
        final JFrame frame = new JFrame("OutManeuver");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);

        final Plane plane = new PlaneImpl(new StandardStats());
        final InputControllerImpl inputCtrl = new InputControllerImpl();
        final HudControllerImpl hudCtrl = new HudControllerImpl();
        final MasterControllerImpl master = new MasterControllerImpl(hudCtrl);
        final EntityControllerImpl entity = new EntityControllerImpl(plane, inputCtrl, master);
        master.setEntityController(entity);

        final SwingGameView gameView = new SwingGameView(new GameKeyListener(inputCtrl, master), new SwingHudView());
        gameView.init();
        master.attachView(gameView);

        final Leaderboard leaderboard = new Leaderboard(
                new JsonLeaderboardRepository(Path.of(System.getProperty("user.home"), ".outmaneuver", "scores.json")));

        final UIManager[] uiManagerRef = { null };

        final GameOverView gameOverView = new GameOverView(
                () -> onPlayAgain(uiManagerRef[0], master, gameView),
                () -> uiManagerRef[0].showScreen(GameState.MENU)
        );
        final MainMenuView mainMenuView = new MainMenuView(
            () -> onStart(uiManagerRef[0], master, gameView),
            () -> System.exit(0)
        );

        // TODO: sostituire con GameEventBus.GAME_OVER quando Spinaci implementa il bus
        master.setOnGameOver(() -> onGameOver(uiManagerRef[0], gameOverView, leaderboard, 0, "Player"));

        final Map<GameState, JPanel> screens = new EnumMap<>(GameState.class);
        screens.put(GameState.MENU, mainMenuView);
        screens.put(GameState.PLAYING, gameView.getPanel());
        screens.put(GameState.PAUSED, gameView.getPanel());
        screens.put(GameState.GAME_OVER, gameOverView);

        final UIManager uiManager = new UIManager(screens);
        uiManager.showScreen(GameState.MENU);
        uiManagerRef[0] = uiManager;

        frame.add(uiManager);
        frame.pack();
        frame.setMinimumSize(frame.getSize());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void onGameOver(final UIManager uiManager,
                                    final GameOverView gameOverView,
                                    final Leaderboard leaderboard,
                                    final int finalScore,
                                    final String playerName) {
        leaderboard.save(finalScore, playerName);
        gameOverView.show(finalScore, leaderboard.getTopScores());
        uiManager.showScreen(GameState.GAME_OVER);
    }

    private static void onStart(final UIManager uiManager,
                                 final MasterController master,
                                 final SwingGameView gameView) {
        uiManager.showScreen(GameState.PLAYING);
        gameView.getPanel().requestFocusInWindow();
        master.start();
    }

    private static void onPlayAgain(final UIManager uiManager,
                                     final MasterController master,
                                     final SwingGameView gameView) {
        uiManager.showScreen(GameState.PLAYING);
        gameView.getPanel().requestFocusInWindow();
        master.start();
    }
}

