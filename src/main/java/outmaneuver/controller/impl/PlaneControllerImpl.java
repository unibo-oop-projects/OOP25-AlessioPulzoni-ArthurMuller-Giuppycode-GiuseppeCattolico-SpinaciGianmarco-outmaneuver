package outmaneuver.controller.impl;

import java.util.List;
import java.util.Objects;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import outmaneuver.controller.CollisionEngine;
import outmaneuver.controller.InputController;

import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.plane.Plane;

public final class PlaneControllerImpl extends EntityControllerImpl {

    private final InputController inputController;
    private Plane plane;
    private double speedMutltiplier = 1.0;

    public PlaneControllerImpl(final InputController inputController,
            final List<Entity> entities,
            final CollisionEngine collisionEngine) {
        super(entities, collisionEngine);
        this.inputController = Objects.requireNonNull(inputController);
    }

    @Override
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "the controller must mutate the very Plane instance it is given each frame")
    public void spawnEntity(final Entity entity) {
        if (entity instanceof final Plane p) {
            plane = p;
            p.reset();
        }
        super.spawnEntity(entity);
    }

    @Override
    public void clearAll() {
        if (plane != null) {
            spawnEntity(plane);
        }
    }

    @Override
    public void updateEntities(final long deltaMs) {
        if (plane == null) {
            return;
        }
        plane.update(deltaMs / 1000.0, inputController.getTurnDirection(), speedMutltiplier);
    }

    public void setSpeedMultiplier(final double multiplier) {
        this.speedMutltiplier = multiplier;
    }
}
