package outmaneuver.controller.impl;

import java.util.List;
import java.util.Objects;

import outmaneuver.controller.CollisionEngine;
import outmaneuver.controller.EntityController;
import outmaneuver.controller.InternalEvent;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.area.entity.plane.TurnState;
import outmaneuver.model.area.collision.CollisionData;
import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.collectibles.Collectible;
import outmaneuver.model.area.entity.missile.Missile;
import outmaneuver.model.session.IGameSession;
import outmaneuver.util.Vector2;
import outmaneuver.view.GameView;

public abstract class EntityControllerImpl implements EntityController {

    private final List<Entity> entities;
    private final CollisionEngine collisionEngine;
    private final IGameSession session;
    private GameView view;

    protected EntityControllerImpl(final List<Entity> entities,
                                final CollisionEngine collisionEngine,
                                final IGameSession session) {
        this.entities = Objects.requireNonNull(entities, "entities must not be null");
        this.collisionEngine = Objects.requireNonNull(collisionEngine, "collisionEngine must not be null");
        this.session = Objects.requireNonNull(session, "session must not be null");
    }

    public void setView(final GameView view) {
        this.view = view;
    }

    protected GameView getView() {
        return view;
    }

    @Override
    public void updateEntities(final long deltaMs) {
        // ogni controller implementa il proprio updateEntities
    }

    @Override
    public void spawnEntity(final Entity entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        entities.add(entity);
        collisionEngine.register(entity);
    }
    

    // Rimozione

    @Override
    public void removeEntity(final Entity entity) {
        if (!(entity instanceof Plane)) {
            collisionEngine.unregister(entity);
            entities.remove(entity);
        }
        // PLANE non viene mai rimosso
    }

    @Override
    public void clearAll() {
        entities.removeIf(e -> {
            if (e instanceof Plane) {
                planeReset((Plane) e);
                return false; // tieni il piano
            }
            if (!(e instanceof Plane)) collisionEngine.unregister(e);
            return true; // rimuovi tutto il resto
        });
    }

    private void planeReset(final Plane plane) {
        plane.setPosition(Vector2.ZERO);
        plane.setDirection(0);
        plane.setTurnState(TurnState.NONE);
    }

    @Override
    public List<Entity> getEntities() { return List.copyOf(entities); }


    @Override
    public void onInternalEvent(final InternalEvent evt, final Object data) {
        // No entity-specific events to handle for now
        switch (evt) {
            case PLANE_MISSILE_COLLISION -> {
                if (data instanceof final CollisionData collisionData) {
                    if (collisionData.getEntityA() instanceof Missile && collisionData.getEntityB() instanceof Plane) {
                        removeEntity((Missile) collisionData.getEntityA());
                        // Notify views or other controllers if needed
                    }
                }
            }
            case PLANE_COLLECTIBLE_COLLISION -> {
                if (data instanceof final CollisionData collisionData) {
                    if (collisionData.getEntityA() instanceof Plane && collisionData.getEntityB() instanceof Collectible) {
                        final Plane plane = (Plane) collisionData.getEntityA();
                        final Collectible collectible = (Collectible) collisionData.getEntityB();
                        collectible.apply(plane, session);
                        removeEntity(collectible);
                        // Notify views or other controllers if needed
                    }
                }
            }
            case MISSILE_MISSILE_COLLISION -> {
                if (data instanceof final CollisionData collisionData) {
                    if (collisionData.getEntityA() instanceof Missile && collisionData.getEntityB() instanceof Missile) {
                        removeEntity((Missile) collisionData.getEntityA());
                        removeEntity((Missile) collisionData.getEntityB());
                        // Notify views or other controllers if needed
                    }
                }
            }
            
        }
    }
}
