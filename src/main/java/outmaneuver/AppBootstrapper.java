package outmaneuver;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.JFrame;

import outmaneuver.assembler.ControllerAssembler;
import outmaneuver.assembler.ScreenAssembler;
import outmaneuver.model.area.entity.missile.data.JsonMissileRepository;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.model.area.entity.missile.data.MissileRepository;
import outmaneuver.model.area.entity.plane.JsonPlaneRepository;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.area.entity.plane.PlaneData;
import outmaneuver.model.area.entity.plane.PlaneImpl;
import outmaneuver.model.area.entity.plane.PlaneRepository;
import outmaneuver.model.profile.IPlayerProfileRepository;
import outmaneuver.model.profile.JsonPlayerProfileRepository;
import outmaneuver.model.profile.PlayerProfile;
import outmaneuver.model.session.ISession;
import outmaneuver.model.session.Session;
import outmaneuver.model.shop.IShop;
import outmaneuver.model.shop.Shop;
import outmaneuver.util.json.GsonProvider;
import outmaneuver.util.json.JsonResourceLoader;
import outmaneuver.view.swing.ScreenId;
import outmaneuver.view.swing.UIManager;

/** Wires up all the game's collaborators and shows the main application window. */
public final class AppBootstrapper {

    private AppBootstrapper() {
    }

    /**
     * Assembles the game's model, controllers and UI, then shows the main window.
     */
    public static void launch() {
        final PlaneRepository planeRepo = new JsonPlaneRepository(
                JsonResourceLoader.forList("planes.json", PlaneData.class, GsonProvider.create()));
        final Plane plane = new PlaneImpl(planeRepo.loadById("standard").orElseThrow());

        final MissileRepository missileRepo = new JsonMissileRepository(
                JsonResourceLoader.forList("missiles.json", MissileData.class, GsonProvider.create()));

        final ISession session = new Session();
        final ControllerAssembler.Controllers ctrl = ControllerAssembler.assemble(plane, session, missileRepo);

        final Path profilePath = JsonPlayerProfileRepository.defaultProfilePath();
        final boolean isFirstLaunch = !Files.exists(profilePath);
        final IPlayerProfileRepository profileRepo = JsonPlayerProfileRepository.create(profilePath);
        final PlayerProfile profile = new PlayerProfile(profileRepo);

        final IShop shop = new Shop(planeRepo);

        final UIManager[] uiRef = {null};
        final ScreenAssembler.Result result = ScreenAssembler.build(ctrl, profile, plane, shop, session, uiRef);

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
