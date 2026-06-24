package outmaneuver.factory;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.JPanel;

import outmaneuver.controller.MasterController;
import outmaneuver.controller.event.GameEvent;
import outmaneuver.controller.impl.MasterControllerImpl;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.profile.PlayerProfile;
import outmaneuver.model.session.IScoreSession;
import outmaneuver.model.shop.IShop;
import outmaneuver.util.assets.AssetStore;
import outmaneuver.util.assets.ClasspathAssetStore;
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

    /** Aspect ratio of the game world (1400 × 1000 = 1.4 : 1). */
    private static final double ASPECT_RATIO = 1400.0 / 1000.0;

    /**
     * Maximum fraction of the screen that the game window may occupy on each axis.
     * 0.9 leaves a small margin so the window fits comfortably inside the desktop.
     */
    private static final double SCREEN_FILL_FACTOR = 0.9;

    private ScreenFactory() { }

    /**
     * Carries the assembled screen map and the game view (needed by the caller to
     * request focus).
     */
    public record Result(Map<ScreenId, JPanel> screens, SwingGameView gameView) { }

    /**
     * Computes a game-window size that:
     * <ol>
     *   <li>Preserves the original 1.4 : 1 aspect ratio.</li>
     *   <li>Scales proportionally to the usable screen area.</li>
     *   <li>Never exceeds {@value #SCREEN_FILL_FACTOR} of the usable area on either axis.</li>
     * </ol>
     * Uses {@link GraphicsEnvironment#getMaximumWindowBounds()} which already
     * excludes OS chrome (taskbar on Windows, Dock on macOS) and returns
     * device-independent pixels, so this works correctly on HiDPI displays too.
     */
    private static Dimension scaledGameSize() {
        final Rectangle screenBounds = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();

        final int maxWidth  = (int) (screenBounds.width  * SCREEN_FILL_FACTOR);
        final int maxHeight = (int) (screenBounds.height * SCREEN_FILL_FACTOR);

        // Fit inside maxWidth × maxHeight while keeping the aspect ratio.
        int width  = maxWidth;
        int height = (int) Math.round(width / ASPECT_RATIO);

        if (height > maxHeight) {
            height = maxHeight;
            width  = (int) Math.round(height * ASPECT_RATIO);
        }

        return new Dimension(width, height);
    }

    public static Result build(
            final ControllerAssembler.Controllers ctrl,
            final PlayerProfile profile,
            final Plane plane,
            final IShop shop,
            final IScoreSession session,
            final UIManager[] uiRef) {

        final MasterControllerImpl master = ctrl.master();

        // L'asset store carica tutti gli sprite una volta sola (cache eager) e li fornisce
        // alla view: dipendenza iniettata dall'esterno, la view non sa COME sono caricati.
        final AssetStore assets = new ClasspathAssetStore();
        final SwingGameView gameView = new SwingGameView(
                new GameKeyListener(ctrl.input(), master),
                new SwingHudView(),
                assets);
        gameView.setPreferredSize(scaledGameSize());
        gameView.init();
        master.attachView(gameView);

        // Leaderboard ref needed for the refresh callback in MainMenuView
        final LeaderboardView[] leaderboardRef = { null };

        final ShopView shopView = new ShopView(
                assets,
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
                () -> master.handleEvent(GameEvent.PAUSED),
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
            final IScoreSession session) {
        session.reset();
        uiManager.showScreen(ScreenId.PLAYING);
        gameView.requestFocusInWindow();
        master.start();
    }
}