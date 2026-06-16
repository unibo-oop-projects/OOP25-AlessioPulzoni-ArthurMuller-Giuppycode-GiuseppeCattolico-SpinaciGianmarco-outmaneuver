package outmaneuver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

import outmaneuver.controller.MasterController;
import outmaneuver.controller.OutmaneuverEvent;
import outmaneuver.controller.impl.EntityControllerImpl;
import outmaneuver.controller.impl.HudControllerImpl;
import outmaneuver.controller.impl.InputControllerImpl;
import outmaneuver.controller.impl.MasterControllerImpl;
import outmaneuver.util.json.GsonProvider;
import outmaneuver.util.json.JsonResourceLoader;
import outmaneuver.model.area.entity.plane.JsonPlaneRepository;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.area.entity.plane.PlaneData;
import outmaneuver.model.area.entity.plane.PlaneImpl;
import outmaneuver.model.area.entity.plane.PlaneRepository;
import outmaneuver.model.profile.IPlayerProfileRepository;
import outmaneuver.model.profile.JsonPlayerProfileRepository;
import outmaneuver.model.profile.PlayerProfile;
import outmaneuver.model.shop.IShop;
import outmaneuver.model.shop.Shop;
import outmaneuver.model.shop.ShopItem;
import outmaneuver.view.swing.GameKeyListener;
import outmaneuver.view.swing.SwingGameView;
import outmaneuver.view.swing.leaderboard.LeaderboardView;
import outmaneuver.view.swing.pause.PauseView;
import outmaneuver.view.swing.ScreenId;
import outmaneuver.view.swing.UIManager;
import outmaneuver.view.swing.gameover.GameOverView;
import outmaneuver.view.swing.hud.SwingHudView;
import outmaneuver.view.swing.menu.MainMenuView;
import outmaneuver.view.swing.setup.UsernameSetupView;
import outmaneuver.view.swing.shop.ShopView;

public final class AppBootstrapper {

    private AppBootstrapper() { }

    public static void launch() {
        final JFrame frame = new JFrame("OutManeuver");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);

        final PlaneRepository planeRepo = new JsonPlaneRepository(
                JsonResourceLoader.forList("planes.json", PlaneData.class, GsonProvider.create()));
        final Plane plane = new PlaneImpl(planeRepo.loadById("standard").orElseThrow());
        final InputControllerImpl inputCtrl = new InputControllerImpl();
        final HudControllerImpl hudCtrl = new HudControllerImpl();
        final MasterControllerImpl master = new MasterControllerImpl(hudCtrl);
        final EntityControllerImpl entity = new EntityControllerImpl(plane, inputCtrl, master);
        master.setEntityController(entity);

        final SwingGameView gameView = new SwingGameView(new GameKeyListener(inputCtrl, master), new SwingHudView());
        gameView.setPreferredSize(new java.awt.Dimension(800, 600));
        gameView.init();
        master.attachView(gameView);

        final Path profilePath = Path.of(System.getProperty("user.home"), ".outmaneuver", "profile.json");
        final boolean isFirstLaunch = !Files.exists(profilePath);
        final IPlayerProfileRepository profileRepo =
                JsonPlayerProfileRepository.create(profilePath);
        final PlayerProfile profile = new PlayerProfile(profileRepo);

        final IShop shop = new Shop(
                planeRepo.loadAll().stream()
                        .map(p -> new ShopItem(p, p.price()))
                        .toList());

        final UIManager[] uiManagerRef       = { null };
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
                () -> uiManagerRef[0].showScreen(ScreenId.MENU)
        );

        final GameOverView gameOverView = new GameOverView(
                () -> onPlayAgain(uiManagerRef[0], master, gameView),
                () -> uiManagerRef[0].showScreen(ScreenId.MENU)
        );
        final LeaderboardView leaderboardView = new LeaderboardView(
                profile::getTopScores,
                () -> uiManagerRef[0].showScreen(ScreenId.MENU)
        );
        leaderboardRef[0] = leaderboardView;

        final MainMenuView mainMenuView = new MainMenuView(
                profile::getPlayerName,
                profile::getCoins,
                () -> plane.getStats().getId(),
                () -> onStart(uiManagerRef[0], master, gameView),
                () -> {
                    shopView.refreshCoins();
                    uiManagerRef[0].showScreen(ScreenId.SHOP);
                },
                () -> {
                    leaderboardRef[0].refresh();
                    uiManagerRef[0].showScreen(ScreenId.LEADERBOARD);
                },
                () -> System.exit(0)
        );

        // TODO: sostituire con GameEventBus.GAME_OVER quando Spinaci implementa il bus
        master.setOnGameOver(() -> onGameOver(uiManagerRef[0], gameOverView, profile, 0));

        master.setOnPause(() -> uiManagerRef[0].showScreen(ScreenId.PAUSED));
        master.setOnResume(() -> {
            uiManagerRef[0].showScreen(ScreenId.PLAYING);
            gameView.requestFocusInWindow();
        });

        final PauseView pauseView = new PauseView(
                () -> master.handleEvent(OutmaneuverEvent.TOGGLE_PAUSE),
                () -> {
                    master.stop();
                    uiManagerRef[0].showScreen(ScreenId.MENU);
                }
        );

        final Map<ScreenId, JPanel> screens = new EnumMap<>(ScreenId.class);
        screens.put(ScreenId.SETUP, new UsernameSetupView(name -> {
            profile.setPlayerName(name);
            uiManagerRef[0].showScreen(ScreenId.MENU);
        }));
        screens.put(ScreenId.MENU, mainMenuView);
        screens.put(ScreenId.PLAYING, gameView);
        screens.put(ScreenId.PAUSED, pauseView);
        screens.put(ScreenId.GAME_OVER, gameOverView);
        screens.put(ScreenId.SHOP, shopView);
        screens.put(ScreenId.LEADERBOARD, leaderboardView);

        final UIManager uiManager = new UIManager(screens);
        uiManager.showScreen(isFirstLaunch ? ScreenId.SETUP : ScreenId.MENU);
        uiManagerRef[0] = uiManager;

        frame.add(uiManager);
        frame.pack();
        frame.setMinimumSize(frame.getSize());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void onGameOver(final UIManager uiManager,
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

    private static void onStart(final UIManager uiManager,
                                 final MasterController master,
                                 final SwingGameView gameView) {
        uiManager.showScreen(ScreenId.PLAYING);
        gameView.requestFocusInWindow();
        master.start();
    }

    private static void onPlayAgain(final UIManager uiManager,
                                     final MasterController master,
                                     final SwingGameView gameView) {
        uiManager.showScreen(ScreenId.PLAYING);
        gameView.requestFocusInWindow();
        master.start();
    }
}

