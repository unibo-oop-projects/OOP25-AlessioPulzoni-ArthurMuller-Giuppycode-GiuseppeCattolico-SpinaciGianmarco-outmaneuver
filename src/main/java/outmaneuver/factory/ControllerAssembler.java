package outmaneuver.factory;

import java.util.ArrayList;
import java.util.List;

import outmaneuver.controller.CollisionEngine;
import outmaneuver.controller.event.GameEvent;
import outmaneuver.controller.impl.CollectibleControllerImpl;
import outmaneuver.controller.impl.GameEventControllerImpl;
import outmaneuver.controller.impl.HudControllerImpl;
import outmaneuver.controller.impl.InputControllerImpl;
import outmaneuver.controller.impl.MasterControllerImpl;
import outmaneuver.controller.impl.missile.MissileControllerImpl;
import outmaneuver.controller.impl.missile.MissileSpawnDirector;
import outmaneuver.controller.impl.PlaneControllerImpl;
import outmaneuver.controller.impl.RenderStateAssemblerImpl;
import outmaneuver.controller.impl.ScoreControllerImpl;
import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.missile.data.JsonMissileRepository;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.model.area.entity.missile.data.MissileRepository;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.session.GameSession;
import outmaneuver.util.json.GsonProvider;
import outmaneuver.util.json.JsonResourceLoader;

/**
 * Assembles and wires all game controllers into a ready-to-use bundle.
 */
public final class ControllerAssembler {

    private ControllerAssembler() {
    }

    /**
     * Immutable bundle of the assembled controllers.
     */
    public record Controllers(
            InputControllerImpl input,
            HudControllerImpl hud,
            MasterControllerImpl master) {
    }

    /**
     * Creates every controller, wires them together, and returns the bundle.
     */
    public static Controllers assemble(final Plane plane, final GameSession session) {
        final InputControllerImpl input = new InputControllerImpl();
        final HudControllerImpl hud = new HudControllerImpl();
        final MasterControllerImpl master = new MasterControllerImpl();
        final CollisionEngine collision = new CollisionEngine(master);
        final ScoreControllerImpl score = new ScoreControllerImpl(session);

        final List<Entity> sharedEntities = new ArrayList<>();
        final PlaneControllerImpl planeCtrl = new PlaneControllerImpl(input, sharedEntities, collision);
        final CollectibleControllerImpl collectibleCtrl = new CollectibleControllerImpl(
                sharedEntities, collision);
        // [Alessio - missili] carica i dati dal JSON e crea il controller dei missili (repository + director)
        final MissileRepository missileRepo = new JsonMissileRepository(
                JsonResourceLoader.forList("missiles.json", MissileData.class, GsonProvider.create()));
        final MissileControllerImpl missileCtrl = new MissileControllerImpl(
                sharedEntities, collision, missileRepo, new MissileSpawnDirector());
                
        // [Alessio - missili] registra il controller dei missili nel master
        planeCtrl.spawnEntity(plane); //TODO: QUESTO NON VA BENE QUI, IL PLANE VA SPAWNATO ALTROVE
        
        master.addEntityController(planeCtrl);
        master.addEntityController(collectibleCtrl);
        master.addEntityController(missileCtrl);
        master.setCollisionEngine(collision);
        master.setScoreController(score); // va qui?
        master.setSceneEntities(sharedEntities);
        master.setStateAssembler(new RenderStateAssemblerImpl(hud)); // TODO: prender Hud, fix temporaneo, spostare
        master.setEventController(new GameEventControllerImpl(
                planeCtrl, hud, score, session,
                () -> master.handleEvent(GameEvent.GAME_OVER)));
        return new Controllers(input, hud, master);
    }
}
