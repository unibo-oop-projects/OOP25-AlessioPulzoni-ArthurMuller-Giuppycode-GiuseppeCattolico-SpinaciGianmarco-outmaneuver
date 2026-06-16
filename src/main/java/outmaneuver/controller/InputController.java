package outmaneuver.controller;

public interface InputController {

    void onKeyPressed(int keyCode);

    void onKeyReleased(int keyCode);

    double getTurnDirection();

}
