package outmaneuver;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.JFrame;

import outmaneuver.factory.ControllerAssembler;
import outmaneuver.factory.ScreenFactory;
import outmaneuver.model.area.entity.plane.JsonPlaneRepository;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.area.entity.plane.PlaneData;
import outmaneuver.model.area.entity.plane.PlaneImpl;
import outmaneuver.model.area.entity.plane.PlaneRepository;
import outmaneuver.model.profile.IPlayerProfileRepository;
import outmaneuver.model.profile.JsonPlayerProfileRepository;
import outmaneuver.model.profile.PlayerProfile;
import outmaneuver.model.session.GameSession;
import outmaneuver.model.shop.IShop;
import outmaneuver.model.shop.Shop;
import outmaneuver.model.shop.ShopItem;
import outmaneuver.util.json.GsonProvider;
import outmaneuver.util.json.JsonResourceLoader;
import outmaneuver.view.swing.ScreenId;
import outmaneuver.view.swing.UIManager;

public final class AppBootstrapper {

    private AppBootstrapper() { }

    public static void launch() {
        final PlaneRepository planeRepo = new JsonPlaneRepository(
                JsonResourceLoader.forList("planes.json", PlaneData.class, GsonProvider.create()));
        final Plane plane = new PlaneImpl(planeRepo.loadById("standard").orElseThrow());

        final GameSession session = new GameSession();
        final ControllerAssembler.Controllers ctrl = ControllerAssembler.assemble(plane, session);

        final Path profilePath = JsonPlayerProfileRepository.defaultProfilePath();
        final boolean isFirstLaunch = !Files.exists(profilePath);
        final IPlayerProfileRepository profileRepo = JsonPlayerProfileRepository.create(profilePath);
        final PlayerProfile profile = new PlayerProfile(profileRepo);

        final IShop shop = new Shop(
                planeRepo.loadAll().stream()
                        .map(p -> new ShopItem(p, p.price()))
                        .toList());

        final UIManager[] uiRef = { null };
        final ScreenFactory.Result result = ScreenFactory.build(ctrl, profile, plane, shop, session, uiRef);

        final UIManager uiManager = new UIManager(result.screens());
        uiRef[0] = uiManager;
        uiManager.showScreen(isFirstLaunch ? ScreenId.SETUP : ScreenId.MENU);

        final JFrame frame = new JFrame("OutManeuver");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.add(uiManager);
        frame.pack();
        frame.setMinimumSize(frame.getSize());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

