package outmaneuver.controller;

import outmaneuver.model.area.entity.plane.Plane;
import java.util.List;

import outmaneuver.controller.event.InternalEventListener;
import outmaneuver.model.Entity;


public interface EntityController extends InternalEventListener {

    void updateEntities(long deltaMs);

    void clearAll();

    void spawnPlane(Entity plane);

    void spawnMissile(Entity missile);

    void spawnCollectible(Entity collectible);

    void removeEntity(Entity entity);

    List<Entity> getEntities();

    Plane getPlane();
}
