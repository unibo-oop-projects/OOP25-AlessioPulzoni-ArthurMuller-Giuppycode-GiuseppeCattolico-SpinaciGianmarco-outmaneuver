package outmaneuver.controller.impl;

import outmaneuver.controller.EntityController;
import outmaneuver.controller.InternalEvent;
import outmaneuver.controller.InputController;
import outmaneuver.model.area.Plane;
import outmaneuver.model.area.TurnState;
import outmaneuver.util.Vector2;

import java.util.Objects;
import java.util.function.BiConsumer;

public final class EntityControllerImpl implements EntityController {

    private final Plane plane;
    private final InputController inputController;
    private final BiConsumer<InternalEvent, Object> eventCallback;
    private final double areaWidth;
    private final double areaHeight;

    public EntityControllerImpl(final Plane plane,
                                final InputController inputController,
                                final BiConsumer<InternalEvent, Object> eventCallback,
                                final double areaWidth,
                                final double areaHeight) {
        this.plane = Objects.requireNonNull(plane, "plane must not be null");
        this.inputController = Objects.requireNonNull(inputController, "inputController must not be null");
        this.eventCallback = Objects.requireNonNull(eventCallback, "eventCallback must not be null");
        this.areaWidth = areaWidth;
        this.areaHeight = areaHeight;
    }

    @Override
    public void updateEntities(final long deltaMs) {
        final double deltaSec = deltaMs / 1000.0;
        final double turnDir = inputController.getTurnDirection();

        plane.setTurnState(turnDir < 0 ? TurnState.LEFT
                : turnDir > 0 ? TurnState.RIGHT
                : TurnState.NONE);

        final double newDir = plane.getDirection() + turnDir * plane.getStats().getTurnRate() * deltaSec;
        plane.setDirection(normaliseAngle(newDir));

        final Vector2 velocity = Vector2.fromAngle(plane.getDirection())
                .scale(plane.getEffectiveSpeed());
        final Vector2 newPos = plane.getPosition().add(velocity.scale(deltaSec));
        final Vector2 clampedPos = clampToBounds(newPos, plane.getStats().getHitboxRadius());
        plane.setPosition(clampedPos);
    }

    @Override
    public void clearAll() {
        plane.setPosition(Vector2.ZERO);
        plane.setDirection(0);
        plane.setTurnState(TurnState.NONE);
    }

    @Override
    public Plane getPlane() {
        return plane;
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

    private Vector2 clampToBounds(final Vector2 position, final double hitboxRadius) {
        final double min = hitboxRadius;
        final double maxX = areaWidth - hitboxRadius;
        final double maxY = areaHeight - hitboxRadius;
        final double clampedX = Math.max(min, Math.min(position.getX(), maxX));
        final double clampedY = Math.max(min, Math.min(position.getY(), maxY));
        return new Vector2(clampedX, clampedY);
    }
}
