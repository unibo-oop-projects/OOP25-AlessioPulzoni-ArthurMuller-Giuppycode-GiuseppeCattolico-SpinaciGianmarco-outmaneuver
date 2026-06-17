package outmaneuver.factory;

import outmaneuver.controller.CollisionEngine;
import outmaneuver.controller.impl.EntityControllerImpl;
import outmaneuver.controller.impl.HudControllerImpl;
import outmaneuver.controller.impl.InputControllerImpl;
import outmaneuver.controller.impl.MasterControllerImpl;
import outmaneuver.controller.impl.ScoreControllerImpl;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.session.GameSession;

/**
 * Assembles and wires all game controllers into a ready-to-use bundle.
 */
public final class ControllerAssembler {

    private ControllerAssembler() { }

    /**
     * Immutable bundle of the assembled controllers.
     */
    public record Controllers(
        InputControllerImpl input,
        HudControllerImpl hud,
        MasterControllerImpl master
    ) { }

    /**
     * Creates every controller, wires them together, and returns the bundle.
     */
    public static Controllers assemble(final Plane plane, final GameSession session) {
        final InputControllerImpl input = new InputControllerImpl();
        final HudControllerImpl hud = new HudControllerImpl();
        final MasterControllerImpl master = new MasterControllerImpl(hud);
        final CollisionEngine collision = new CollisionEngine(master);
        final EntityControllerImpl entity = new EntityControllerImpl(input, master, collision, session);
        entity.spawnPlane(plane);
        master.setEntityController(entity);
        master.setCollisionEngine(collision);
        master.setScoreController(new ScoreControllerImpl(session));
        return new Controllers(input, hud, master);
    }
}
