package outmaneuver.controller;

import outmaneuver.view.GameView;

public interface MasterController {

    void handleEvent(OutmaneuverEvent event);

    void attachView(GameView view);

    void start();

    void stop();
}
