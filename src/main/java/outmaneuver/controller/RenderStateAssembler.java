package outmaneuver.controller;

import java.util.List;

import outmaneuver.model.area.entity.Entity;
import outmaneuver.view.RenderState;

public interface RenderStateAssembler {

    RenderState assemble(List<Entity> entities, boolean paused,
            long elapsedMs, int stars, double speedMultiplier, boolean shieldActive);
}
