package outmaneuver.controller;

import outmaneuver.model.area.entity.plane.Plane;

public interface EntityController {

    void updateEntities(long deltaMs);

    void clearAll();

    Plane getPlane();
}
