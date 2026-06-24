package outmaneuver.controller.impl;

import java.util.List;
import java.util.Objects;

import outmaneuver.controller.CollisionEngine;
import outmaneuver.controller.InputController;

import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.area.entity.plane.TurnState;
import outmaneuver.util.Vector2;

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
    public void spawnEntity(final Entity entity) {
        if (entity instanceof final Plane p) {
            plane = p;
            planeReset(p);
        }
        super.spawnEntity(entity);
    }

    @Override
    public void clearAll() {
        inputController.reset();
        removeAll();
        if (plane != null) {
            spawnEntity(plane);
        }
    }

    @Override
    public void updateEntities(final long deltaMs) {
        if (plane == null) {
            return;
        }
        final double deltaSec = deltaMs / 1000.0;
        final double turnDir = inputController.getTurnDirection();

        plane.setTurnState(turnDir < 0 ? TurnState.LEFT
                : turnDir > 0 ? TurnState.RIGHT
                        : TurnState.NONE);

        final double newDir = plane.getDirection() + turnDir * plane.getStats().getTurnRate() * deltaSec;
        plane.setDirection(normaliseAngle(newDir));

        final double speed = plane.getStats().getBaseSpeed() * speedMutltiplier;
        final Vector2 velocity = Vector2.fromAngle(plane.getDirection()).scale(speed);
        plane.setPosition(plane.getPosition().add(velocity.scale(deltaSec)));
    }

    public void setSpeedMultiplier(double multiplier) {
        this.speedMutltiplier = multiplier;
    }

    private static double normaliseAngle(final double angle) {
        final double twoPi = 2 * Math.PI;
        double normalised = angle % twoPi;
        if (normalised > Math.PI) {
            normalised -= twoPi;
        } else if (normalised < -Math.PI) {
            normalised += twoPi;
        }
        return normalised;
    }

    protected void planeReset(final Plane plane) {
        plane.setPosition(Vector2.ZERO);
        plane.setDirection(0);
        plane.setTurnState(TurnState.NONE);
    }
}
