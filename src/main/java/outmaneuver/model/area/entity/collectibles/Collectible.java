package outmaneuver.model.area.entity.collectibles;

import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.session.IGameSession;

public interface Collectible extends Entity {

    void apply(Plane plane, IGameSession session);

    /**
     * Tipo di dominio del collectible (es. "star", "speed", "shield"), analogo a
     * {@code Missile.getMissileType()}: identifica il collectible. La view lo mappa allo
     * sprite, cosi' il model non conosce nulla di presentazione.
     */
    String getCollectibleType();
}
