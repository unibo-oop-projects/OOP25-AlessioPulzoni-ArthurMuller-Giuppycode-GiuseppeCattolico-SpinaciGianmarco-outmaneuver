package outmaneuver.controller;

import outmaneuver.controller.event.InternalEventListener;
import outmaneuver.model.area.Plane;
import outmaneuver.view.HudSnapshot;

public interface HudController extends InternalEventListener {

    HudSnapshot buildSnapshot(Plane plane, boolean paused);

    void reset();
}
