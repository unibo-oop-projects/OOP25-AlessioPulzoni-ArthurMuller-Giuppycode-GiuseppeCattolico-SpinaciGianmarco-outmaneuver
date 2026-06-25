package outmaneuver.factory;

import java.util.ArrayList;
import java.util.List;

import outmaneuver.controller.CollisionEngine;
import outmaneuver.controller.event.GameEvent;
import outmaneuver.controller.event.EventController;
import outmaneuver.controller.impl.CollectibleControllerImpl;
import outmaneuver.controller.impl.InputControllerImpl;
import outmaneuver.controller.impl.MasterControllerImpl;
import outmaneuver.controller.impl.missile.MissileControllerImpl;
import outmaneuver.controller.impl.missile.MissileSpawnDirector;
import outmaneuver.controller.impl.PlaneControllerImpl;
import outmaneuver.controller.impl.RenderStateAssemblerImpl;
import outmaneuver.controller.impl.ScoreControllerImpl;
import outmaneuver.controller.impl.SessionState;
import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.missile.data.JsonMissileRepository;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.model.area.entity.missile.data.MissileRepository;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.session.ScoreSession;
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
            MasterControllerImpl master) {
    }

    /**
     * Creates every controller, wires them together, and returns the bundle.
     */
    public static Controllers assemble(final Plane plane, final ScoreSession session) {
        final InputControllerImpl input = new InputControllerImpl();
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
        final SessionState sessionState = new SessionState();
        final EventController eventController = new EventController(
                master, sessionState, score, () -> master.handleEvent(GameEvent.GAME_OVER));
        
        master.setCollisionEngine(collision);
        master.setScoreController(score); // va qui?
        master.setSessionState(sessionState);
        master.setSceneEntities(sharedEntities);
        master.setInputController(input);
        master.setStateAssembler(new RenderStateAssemblerImpl());
        master.setEventController(eventController);
        
        planeCtrl.setEventListener(eventController);
        collectibleCtrl.setEventListener(eventController);
        missileCtrl.setEventListener(eventController);

        return new Controllers(input, master);
    }
}