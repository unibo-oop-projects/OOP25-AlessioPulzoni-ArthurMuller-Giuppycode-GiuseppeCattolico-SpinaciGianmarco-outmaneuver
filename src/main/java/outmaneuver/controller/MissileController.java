package outmaneuver.controller;

import java.util.List;

import outmaneuver.model.area.Plane;
import outmaneuver.model.missile.MissileRenderData;

public interface MissileController {

    void update(Plane plane, double dt);

    List<MissileRenderData> getRenderData();

    void reset();
}