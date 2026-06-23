package outmaneuver.controller;

import outmaneuver.model.area.entity.Entity;

import java.util.List;

import outmaneuver.controller.event.InternalEventListener;
import outmaneuver.view.GameView;


public interface EntityController extends InternalEventListener {

    void updateEntities(long deltaMs);

    void clearAll();

    void spawnEntity(Entity entity);

    void removeEntity(Entity entity);

    void removeAll();

    List<Entity> getEntities();

    default void setView(GameView view) { }

}
