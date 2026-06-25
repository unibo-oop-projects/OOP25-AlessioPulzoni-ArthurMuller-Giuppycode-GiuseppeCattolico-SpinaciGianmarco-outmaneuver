package outmaneuver.controller;

import java.util.List;

import outmaneuver.model.area.entity.Entity;
import outmaneuver.util.Vector2;
import outmaneuver.view.RenderState;

/**
 * Builds an immutable {@link RenderState} snapshot from the current game state, ready
 * to be handed to a view for rendering. Implementers must not hold references to the
 * mutable game model beyond the call to {@link #assemble}.
 */
public interface RenderStateAssembler {

    /**
     * Assembles a render-ready snapshot of the current frame.
     *
     * @param entities the entities currently present in the scene
     * @param paused whether the game is currently paused
     * @param elapsedMs total time elapsed in the session, in milliseconds
     * @param stars the number of stars collected so far
     * @param speedMultiplier the currently active speed multiplier
     * @param shieldActive whether the shield effect is currently active
     * @param collisionPoints the world-space points where collisions occurred this frame
     * @return the assembled render state
     */
    RenderState assemble(List<Entity> entities, boolean paused,
            long elapsedMs, int stars, double speedMultiplier,
            boolean shieldActive, List<Vector2> collisionPoints);

    /** Clears any internal state accumulated across frames (e.g. ongoing explosion animations). */
    void reset();
}
