package outmaneuver.assembler;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.JPanel;

import outmaneuver.controller.event.GameEvent;
import outmaneuver.controller.impl.MasterControllerImpl;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.profile.PlayerProfile;
import outmaneuver.model.session.ISession;
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
public final class ScreenAssembler {

    /** Aspect ratio of the game world (1400 × 1000 = 1.4 : 1). */
    private static final double ASPECT_RATIO = 1400.0 / 1000.0;

    /**
     * Maximum fraction of the screen that the game window may occupy on each axis.
     * 0.9 leaves a small margin so the window fits comfortably inside the desktop.
     */
    private static final double SCREEN_FILL_FACTOR = 0.9;

    private ScreenAssembler() { }

    /**
     * Carries the assembled screen map and the game view (needed by the caller to
     * request focus).
     */
    public record Result(Map<ScreenId, JPanel> screens, SwingGameView gameView) { }

    /**
     * Provides proportional scaling from the reference 1400×1000 game size
     * to the actual panel dimensions.
     */
    public record ScreenMetrics(int width, int height) {
        private static final int REF_W = 1400;
        private static final int REF_H = 1000;

        public double scaleX() { return (double) width / REF_W; }
        public double scaleY() { return (double) height / REF_H; }
        public double scale()  { return Math.min(scaleX(), scaleY()); }

        public int sw(int v) { return (int) Math.round(v * scaleX()); }
        public int sh(int v) { return (int) Math.round(v * scaleY()); }
        public int sf(int v) { return Math.max(12, (int) Math.round(v * scale())); }
    }

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
            final ISession session,
            final UIManager[] uiRef) {

        final MasterControllerImpl master = ctrl.master();

        final Dimension gameSize = scaledGameSize();
        final ScreenMetrics metrics = new ScreenMetrics(gameSize.width, gameSize.height);

        // L'asset store carica tutti gli sprite una volta sola (cache eager) e li fornisce
        // alla view: dipendenza iniettata dall'esterno, la view non sa COME sono caricati.
        final AssetStore assets = new ClasspathAssetStore();
        final SwingGameView gameView = new SwingGameView(
                new GameKeyListener(ctrl.input(), master),
                new SwingHudView(metrics),
                assets);
        gameView.setPreferredSize(gameSize);
        gameView.init();
        master.attachView(gameView);

        master.setPlayerProfile(profile);

        // Leaderboard ref needed for the refresh callback in MainMenuView
        final LeaderboardView[] leaderboardRef = { null };

        final ShopView shopView = new ShopView(
                metrics,
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
                    if (!shop.purchase(item, profile)) {
                        return false;
                    }
                    profile.addOwnedPlane(id);
                    plane.setStats(item.stats());
                    return true;
                },
                () -> uiRef[0].showScreen(ScreenId.MENU)
        );

        final GameOverView gameOverView = new GameOverView(
                metrics,
                () -> {
                    uiRef[0].showScreen(ScreenId.PLAYING);
                    gameView.requestFocusInWindow();
                    master.start();
                },
                () -> uiRef[0].showScreen(ScreenId.MENU)
        );
        final LeaderboardView leaderboardView = new LeaderboardView(
                metrics,
                profile::getTopScores,
                () -> uiRef[0].showScreen(ScreenId.MENU)
        );
        leaderboardRef[0] = leaderboardView;

        final MainMenuView mainMenuView = new MainMenuView(
                metrics,
                profile::getPlayerName,
                profile::getCoins,
                () -> plane.getStats().getId(),
                () -> {
                    uiRef[0].showScreen(ScreenId.PLAYING);
                    gameView.requestFocusInWindow();
                    master.start();
                },
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

        master.setOnGameOver(() -> {
            gameOverView.show(session.getScore(), profile.getTopScores(),
                              session.getStarsScore(), session.getMissilesScore());
            uiRef[0].showScreen(ScreenId.GAME_OVER);
        });
        master.setOnPause(() -> uiRef[0].showScreen(ScreenId.PAUSED));
        master.setOnResume(() -> {
            uiRef[0].showScreen(ScreenId.PLAYING);
            gameView.requestFocusInWindow();
        });

        final PauseView pauseView = new PauseView(
                metrics,
                () -> master.handleEvent(GameEvent.PAUSED),
                () -> {
                    master.stop();
                    uiRef[0].showScreen(ScreenId.MENU);
                }
        );

        final UsernameSetupView setupView = new UsernameSetupView(metrics, name -> {
            profile.setPlayerName(name);
            uiRef[0].showScreen(ScreenId.MENU);
        });

        final Map<ScreenId, JPanel> screens = new EnumMap<>(ScreenId.class);
        screens.put(ScreenId.SETUP, setupView);
        screens.put(ScreenId.MENU, mainMenuView);
        screens.put(ScreenId.PLAYING, gameView);
        screens.put(ScreenId.PAUSED, pauseView);
        screens.put(ScreenId.GAME_OVER, gameOverView);
        screens.put(ScreenId.SHOP, shopView);
        screens.put(ScreenId.LEADERBOARD, leaderboardView);

        return new Result(screens, gameView);
    }

}