package outmaneuver.assembler;

import java.util.ArrayList;
import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.missile.data.MissileRepository;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.session.ISession;

/**
 * Assembles and wires all game controllers into a ready-to-use bundle.
 */
public final class ControllerAssembler {

    private ControllerAssembler() {
    }

    /**
     * Creates every controller, wires them together, and returns the bundle.
     *
     * @param plane the player's plane entity to register with the plane controller
     * @param session the game session used to track score
     * @param missileRepo repository providing missile definitions to the missile controller
     * @return the bundle of wired controllers
     */
    public static Controllers assemble(final Plane plane, final ISession session,
            final MissileRepository missileRepo) {
        final InputControllerImpl input = new InputControllerImpl();
        final MasterControllerImpl master = new MasterControllerImpl();
        final CollisionEngine collision = new CollisionEngine(master);
        final ScoreControllerImpl score = new ScoreControllerImpl(session, master::getTickMs);
        final List<Entity> sharedEntities = new ArrayList<>();
        final PlaneControllerImpl planeCtrl = new PlaneControllerImpl(input, sharedEntities, collision);
        final CollectibleControllerImpl collectibleCtrl = new CollectibleControllerImpl(
                sharedEntities, collision);
        final MissileControllerImpl missileCtrl = new MissileControllerImpl(
                sharedEntities, collision, missileRepo, new MissileSpawnDirector());

        planeCtrl.spawnEntity(plane);

        master.addEntityController(planeCtrl);
        master.addEntityController(collectibleCtrl);
        master.addEntityController(missileCtrl);
        final EventController eventController = new EventController(
                master, score, () -> master.handleEvent(GameEvent.GAME_OVER));

        master.setCollisionEngine(collision);
        master.setScoreController(score);
        master.setSceneEntities(sharedEntities);
        master.setInputController(input);
        master.setStateAssembler(new RenderStateAssemblerImpl());
        master.setEventController(eventController);

        planeCtrl.setEventListener(eventController);
        collectibleCtrl.setEventListener(eventController);
        missileCtrl.setEventListener(eventController);

        return new Controllers(input, master);
    }

    /**
     * Immutable bundle of the assembled controllers.
     *
     * @param input the wired input controller
     * @param master the wired master controller
     */
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP",
            justification = "input/master are live, shared controller singletons wired by the assembler")
    public record Controllers(
            InputControllerImpl input,
            MasterControllerImpl master) {
    }
}
