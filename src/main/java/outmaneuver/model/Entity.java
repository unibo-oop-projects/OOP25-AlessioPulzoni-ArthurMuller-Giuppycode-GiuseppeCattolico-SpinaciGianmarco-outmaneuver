package outmaneuver.model;

import outmaneuver.model.collision.ICollidable;
import outmaneuver.util.Vector2;

public interface Entity extends ICollidable {
    Vector2 getPosition();
    void setPosition(Vector2 position);
}