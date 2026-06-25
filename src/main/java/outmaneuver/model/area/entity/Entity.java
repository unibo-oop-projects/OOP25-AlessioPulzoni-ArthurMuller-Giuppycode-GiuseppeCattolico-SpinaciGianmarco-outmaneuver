package outmaneuver.model.area.entity;

import outmaneuver.model.area.collision.ICollidable;
import outmaneuver.util.Vector2;

/** Base contract for any object that exists in the game area at a given position. */
public interface Entity extends ICollidable {

    /**
     * Returns the entity's current position.
     *
     * @return the position in world coordinates
     */
    Vector2 getPosition();

    /**
     * Moves the entity to the given position.
     *
     * @param position the new position in world coordinates
     */
    void setPosition(Vector2 position);

}
