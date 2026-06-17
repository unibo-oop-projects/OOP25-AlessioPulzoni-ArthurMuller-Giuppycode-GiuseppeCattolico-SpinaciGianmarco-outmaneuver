package outmaneuver.controller.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import outmaneuver.controller.CollisionEngine;
import outmaneuver.controller.EntityController;
import outmaneuver.controller.InputController;
import outmaneuver.controller.InternalEvent;
import outmaneuver.controller.MissileController;
import outmaneuver.controller.event.InternalEventListener;
import outmaneuver.model.area.collision.ICollidable;
import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.collectibles.Collectible;
import outmaneuver.model.area.entity.missile.Missile;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.area.entity.plane.TurnState;
import outmaneuver.model.session.IGameSession;
import outmaneuver.util.Vector2;

public final class EntityControllerImpl implements EntityController {

    private final List<Entity> entities = new ArrayList<>();
    private final InputController inputController;
    private final InternalEventListener eventListener;
    private final CollisionEngine collisionEngine;
    private final IGameSession session;
    private MissileController missileController;

    public EntityControllerImpl(final InputController inputController,
                                final InternalEventListener eventListener,
                                final CollisionEngine collisionEngine,
                                final IGameSession session) {
        this.inputController = Objects.requireNonNull(inputController, "inputController must not be null");
        this.eventListener = Objects.requireNonNull(eventListener, "eventListener must not be null");
        this.collisionEngine = Objects.requireNonNull(collisionEngine, "collisionEngine must not be null");
        this.session = Objects.requireNonNull(session, "session must not be null");
    }

    public void setMissileController(final MissileController missileController) {
        this.missileController = Objects.requireNonNull(missileController, "missileController must not be null");
    }

    @Override
    public void updateEntities(final long deltaMs) {
        final List<Entity> toRemove = new ArrayList<>();

        for (final Entity e : entities) {
            if (e instanceof final Plane plane) {
                updatePlane(plane, deltaMs);
            } else if (e instanceof final Collectible c) {
                checkCollectible(c, toRemove);
            }
        }

        toRemove.forEach(this::removeEntity);
    }


    private void updatePlane(final Plane plane, final long deltaMs) {
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
        plane.setPosition(newPos);

        if (missileController != null) {
            missileController.update(plane, deltaSec);
        }
    }

    private void checkCollectible(final Collectible c, final List<Entity> toRemove) {
        for (final Entity e : entities) {
            if (e instanceof final Plane p && p.getHitbox().intersects(c.getHitbox())) {
                c.apply(p, session);
                toRemove.add(c);
                eventListener.onInternalEvent(InternalEvent.PLANE_COLLECTIBLE_COLLISION, c);
                return;
            }
        }
    }

    @Override
    public void spawnPlane(final Entity plane) {
        addEntity(plane);
    }

    @Override
    public void spawnMissile(final Entity missile) {
        addEntity(missile);
    }

    @Override
    public void spawnCollectible(final Entity collectible) {
        addEntity(collectible);
    }

    private void addEntity(final Entity entity) {
        Objects.requireNonNull(entity);
        entities.add(entity);
        if ((entity instanceof Missile || entity instanceof Plane)
                && entity instanceof ICollidable collidable) {
            collisionEngine.register(collidable);
        }
    }

    @Override
    public void clearAll() {
        entities.removeIf(e -> {
            if (e instanceof Plane) {
                planeReset((Plane) e);
                return false; // tieni il piano
            }
            if (e instanceof Missile && e instanceof ICollidable collidable) {
                collisionEngine.unregister(collidable);
            }
            return true; // rimuovi tutto il resto
        });
        if (missileController != null) {
            missileController.reset();
        }
    }

    private void planeReset(final Plane plane) {
        plane.setPosition(Vector2.ZERO);
        plane.setDirection(0);
        plane.setTurnState(TurnState.NONE);
    }

    @Override
    public void removeEntity(final Entity entity) {
        if (entity instanceof Missile && entity instanceof ICollidable collidable) {
            collisionEngine.unregister(collidable);
        }
        // PLANE non viene mai rimosso
        if (!(entity instanceof Plane)) {
            entities.remove(entity);
        }
    }

    @Override
    public List<Entity> getEntities() { return List.copyOf(entities); }

    @Override
    public Plane getPlane() {
        return entities.stream()
                .filter(e -> e instanceof Plane)
                .map(e -> (Plane) e)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void onInternalEvent(final InternalEvent evt, final Object data) {
        // No entity-specific events to handle for now
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
}
