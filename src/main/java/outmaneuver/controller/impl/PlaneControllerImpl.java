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
        for (final Entity e : getEntities()) {
            if (e instanceof final Plane p) {
                final double deltaSec = deltaMs / 1000.0;
                final double turnDir = inputController.getTurnDirection();

                p.setTurnState(turnDir < 0 ? TurnState.LEFT
                        : turnDir > 0 ? TurnState.RIGHT
                        : TurnState.NONE);

                final double newDir = p.getDirection() + turnDir * p.getStats().getTurnRate() * deltaSec;
                p.setDirection(normaliseAngle(newDir));

                final Vector2 velocity = Vector2.fromAngle(p.getDirection()).scale(p.getEffectiveSpeed());
                p.setPosition(p.getPosition().add(velocity.scale(deltaSec)));
            }
        }
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
