package outmaneuver.controller.impl;

import java.util.List;
import java.util.Objects;

import outmaneuver.controller.CollisionEngine;
import outmaneuver.controller.EntityController;
import outmaneuver.controller.event.Event;
import outmaneuver.controller.event.InternalEventListener;
import outmaneuver.model.area.entity.Entity;
import outmaneuver.view.GameView;

// CHECKSTYLE: AbstractClassName OFF
/**
 * Base {@link EntityController} implementation shared by concrete controllers
 * (plane, missiles, collectibles): owns the entity list and collision engine
 * registration, and forwards internal events to an optional listener.
 */
public abstract class EntityControllerImpl implements EntityController {
    // CHECKSTYLE: AbstractClassName ON

    private final List<Entity> entities;
    private final CollisionEngine collisionEngine;
    private InternalEventListener eventListener;
    private GameView view;

    /**
     * Creates a controller backed by the given shared entity list and collision engine.
     *
     * @param entities the shared list of entities in the scene
     * @param collisionEngine the collision engine entities register with
     */
    protected EntityControllerImpl(final List<Entity> entities,
                                final CollisionEngine collisionEngine) {
        this.entities = Objects.requireNonNull(entities, "entities must not be null");
        this.collisionEngine = Objects.requireNonNull(collisionEngine, "collisionEngine must not be null");
    }

    /**
     * Stores the game view for subclasses needing screen bounds (via {@link #getView()}).
     *
     * @param view the active game view
     */
    @Override
    public final void setView(final GameView view) {
        this.view = view;
    }

    /**
     * Sets the listener notified of internal events raised by this controller.
     *
     * @param listener the listener to notify
     */
    public final void setEventListener(final InternalEventListener listener) {
        this.eventListener = listener;
    }

    /**
     * Returns the game view previously supplied via {@link #setView}.
     *
     * @return the active game view, or {@code null} if none has been set
     */
    protected final GameView getView() {
        return view;
    }

    @Override
    public void updateEntities(final long deltaMs) {
        // ogni controller implementa il proprio updateEntities
    }

    // CHECKSTYLE: DesignForExtension OFF
    @Override
    public void spawnEntity(final Entity entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        entities.add(entity);
        collisionEngine.register(entity);
    }
    // CHECKSTYLE: DesignForExtension ON

    // Rimozione di una singola entity

    @Override
    public final void removeEntity(final Entity entity) {
            collisionEngine.unregister(entity);
            entities.remove(entity);
    }

    @Override
    public void clearAll() {
        // resettato dal controller specifico
    }

    @Override
    public final void removeAll() {
        entities.forEach(collisionEngine::unregister);
        entities.clear();
    }

    @Override
    public final List<Entity> getEntities() {
        return List.copyOf(entities);
    }

    @Override
    public final void onInternalEvent(final Event evt, final Object data) {
        if (eventListener != null) {
            eventListener.onInternalEvent(evt, data);
        }
    }

}
