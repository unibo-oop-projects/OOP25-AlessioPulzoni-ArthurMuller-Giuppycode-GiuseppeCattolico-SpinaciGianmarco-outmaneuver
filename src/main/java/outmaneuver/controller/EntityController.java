package outmaneuver.controller;

import outmaneuver.model.area.entity.Entity;

import java.util.List;

import outmaneuver.controller.event.InternalEventListener;
import outmaneuver.view.GameView;

/**
 * Manages a category of entities (e.g. planes, missiles, collectibles) within the
 * game scene: spawning, removing and advancing them each tick. Implementers register
 * their entities with the collision engine and react to internal events relevant to
 * that entity category.
 */
public interface EntityController extends InternalEventListener {

    /**
     * Advances all managed entities by one game tick.
     *
     * @param deltaMs milliseconds elapsed since the previous tick
     */
    void updateEntities(long deltaMs);

    /** Resets controller-specific state kept across entities, without necessarily removing them. */
    void clearAll();

    /**
     * Adds an entity to this controller and registers it with the collision engine.
     *
     * @param entity the entity to spawn
     */
    void spawnEntity(Entity entity);

    /**
     * Removes a single entity from this controller and unregisters it from the collision engine.
     *
     * @param entity the entity to remove
     */
    void removeEntity(Entity entity);

    /** Removes all managed entities and unregisters them from the collision engine. */
    void removeAll();

    /**
     * Returns an immutable snapshot of the entities currently managed by this controller.
     *
     * @return the list of managed entities
     */
    List<Entity> getEntities();

    /**
     * Supplies the game view to controllers that need it (e.g. for screen bounds). Default
     * implementation is a no-op for controllers that don't need a view reference.
     *
     * @param view the active game view
     */
    default void setView(final GameView view) { }

}
