package outmaneuver.controller;

import outmaneuver.model.area.Plane;

public interface EntityController {

    void updateEntities(long deltaMs);

    void clearAll();

    Plane getPlane();
}
