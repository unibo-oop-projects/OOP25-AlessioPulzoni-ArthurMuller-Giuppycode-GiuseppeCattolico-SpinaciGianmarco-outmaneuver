package outmaneuver.factory;

import java.awt.Dimension;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.JPanel;

import outmaneuver.controller.MasterController;
import outmaneuver.controller.OutmaneuverEvent;
import outmaneuver.controller.impl.MasterControllerImpl;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.profile.PlayerProfile;
import outmaneuver.model.session.IGameSession;
import outmaneuver.model.shop.IShop;
import outmaneuver.view.swing.GameKeyListener;
import outmaneuver.view.swing.ScreenId;
import outmaneuver.view.swing.SwingGameView;
import outmaneuver.view.swing.UIManager;
import outmaneuver.view.swing.gameover.GameOverView;
import outmaneuver.view.swing.hud.SwingHudView;
import outmaneuver.view.swing.leaderboard.LeaderboardView;
import outmaneuver.view.swing.menu.MainMenuView;
import outmaneuver.view.swing.pause.PauseView;
import outmaneuver.view.swing.setup.UsernameSetupView;
import outmaneuver.view.swing.shop.ShopView;

/**
 * Builds all Swing screens, wires their navigation callbacks, and returns the
 * complete screen map together with the {@link SwingGameView}.
 */
public final class ScreenFactory {

    private static final int GAME_WIDTH  = 800;
    private static final int GAME_HEIGHT = 600;

    private ScreenFactory() { }

    /**
     * Carries the assembled screen map and the game view (needed by the caller to
     * request focus).
     */
    public record Result(Map<ScreenId, JPanel> screens, SwingGameView gameView) { }

    /**
     * Creates every screen, registers navigation callbacks that use {@code uiRef}
     * (a one-element array populated by the caller immediately after this method
     * returns), and configures lifecycle hooks on the master controller.
     */
    public static Result build(
            final ControllerAssembler.Controllers ctrl,
            final PlayerProfile profile,
            final Plane plane,
            final IShop shop,
            final IGameSession session,
            final UIManager[] uiRef) {

        final MasterControllerImpl master = ctrl.master();

        final SwingGameView gameView = new SwingGameView(
                new GameKeyListener(ctrl.input(), master),
                new SwingHudView());
        gameView.setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        gameView.init();
        master.attachView(gameView);

        // Leaderboard ref needed for the refresh callback in MainMenuView
        final LeaderboardView[] leaderboardRef = { null };

        final ShopView shopView = new ShopView(
                shop.getCatalog(),
                profile::getCoins,
                plane::getStats,
                profile::ownsPlane,
                item -> {
                    final String id = item.stats().getId();
                    if (profile.ownsPlane(id)) {
                        plane.setStats(item.stats());
                        return true;
                    }
                    if (!profile.spend(item.price())) {
                        return false;
                    }
                    profile.addOwnedPlane(id);
                    plane.setStats(item.stats());
                    return true;
                },
                () -> uiRef[0].showScreen(ScreenId.MENU)
        );

        final GameOverView gameOverView = new GameOverView(
                () -> onPlayAgain(uiRef[0], master, gameView, session),
                () -> uiRef[0].showScreen(ScreenId.MENU)
        );

        final LeaderboardView leaderboardView = new LeaderboardView(
                profile::getTopScores,
                () -> uiRef[0].showScreen(ScreenId.MENU)
        );
        leaderboardRef[0] = leaderboardView;

        final MainMenuView mainMenuView = new MainMenuView(
                profile::getPlayerName,
                profile::getCoins,
                () -> plane.getStats().getId(),
                () -> onStart(uiRef[0], master, gameView),
                () -> {
                    shopView.refreshCoins();
                    uiRef[0].showScreen(ScreenId.SHOP);
                },
                () -> {
                    leaderboardRef[0].refresh();
                    uiRef[0].showScreen(ScreenId.LEADERBOARD);
                },
                () -> System.exit(0)
        );

        master.setOnGameOver(() -> onGameOver(uiRef[0], gameOverView, profile, session.getScore()));
        master.setOnPause(() -> uiRef[0].showScreen(ScreenId.PAUSED));
        master.setOnResume(() -> {
            uiRef[0].showScreen(ScreenId.PLAYING);
            gameView.requestFocusInWindow();
        });

        final PauseView pauseView = new PauseView(
                () -> master.handleEvent(OutmaneuverEvent.TOGGLE_PAUSE),
                () -> {
                    master.stop();
                    uiRef[0].showScreen(ScreenId.MENU);
                }
        );

        final Map<ScreenId, JPanel> screens = new EnumMap<>(ScreenId.class);
        screens.put(ScreenId.SETUP, new UsernameSetupView(name -> {
            profile.setPlayerName(name);
            uiRef[0].showScreen(ScreenId.MENU);
        }));
        screens.put(ScreenId.MENU, mainMenuView);
        screens.put(ScreenId.PLAYING, gameView);
        screens.put(ScreenId.PAUSED, pauseView);
        screens.put(ScreenId.GAME_OVER, gameOverView);
        screens.put(ScreenId.SHOP, shopView);
        screens.put(ScreenId.LEADERBOARD, leaderboardView);

        return new Result(screens, gameView);
    }

    private static void onGameOver(
            final UIManager uiManager,
            final GameOverView gameOverView,
            final PlayerProfile profile,
            final int finalScore) {
        if (finalScore > 0) {
            profile.addCoins(finalScore);
        }
        profile.saveScore(finalScore, profile.getPlayerName());
        gameOverView.show(finalScore, profile.getTopScores());
        uiManager.showScreen(ScreenId.GAME_OVER);
    }

    private static void onStart(
            final UIManager uiManager,
            final MasterController master,
            final SwingGameView gameView) {
        uiManager.showScreen(ScreenId.PLAYING);
        gameView.requestFocusInWindow();
        master.start();
    }

    private static void onPlayAgain(
            final UIManager uiManager,
            final MasterController master,
            final SwingGameView gameView,
            final IGameSession session) {
        session.reset();
        uiManager.showScreen(ScreenId.PLAYING);
        gameView.requestFocusInWindow();
        master.start();
    }
}
