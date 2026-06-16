package outmaneuver.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import outmaneuver.controller.event.InternalEventListener;
import outmaneuver.model.collision.CollisionData;
import outmaneuver.model.collision.CollisionLayer;
import outmaneuver.model.collision.Hitbox;
import outmaneuver.model.collision.ICollidable;
import outmaneuver.util.Vector2;

public class CollisionEngine {

     private final InternalEventListener eventListener;

    /** Entità attive nella scena corrente. */
    private final List<ICollidable> entities = new ArrayList<>();

    public CollisionEngine(final InternalEventListener eventListener) {
        this.eventListener = Objects.requireNonNull(eventListener,
                "eventListener must not be null");
    }

    // Registrazione entità (chiamato da EntityController / GameScene)
    
    public void register(final ICollidable entity) {
        entities.add(Objects.requireNonNull(entity));
    }

    public void unregister(final ICollidable entity) {
        entities.remove(entity);
    }

    public void clearAll() {
        entities.clear();
    }
    // Tick — chiamato ogni frame dal game loop (game loop thread)

    /**
     * Itera su tutte le coppie rilevanti e verifica l'intersezione delle hitbox.
     * Quando rileva un hit chiama {@code eventListener.onInternalEvent()} con
     * l'evento appropriato e un {@link CollisionData} come payload.
     */
    public void tick() {
        final List<ICollidable> missiles = filterByLayer(CollisionLayer.MISSILE);
        final List<ICollidable> planes   = filterByLayer(CollisionLayer.PLANE);

        // Missile × Missile
        checkPairs(missiles, missiles, InternalEvent.MISSILE_MISSILE_COLLISION);

        // Missile × Plane
     // plane x collectible
        checkPairs(missiles, planes, InternalEvent.PLANE_MISSILE_COLLISION);
    }
     
    private List<ICollidable> filterByLayer(final CollisionLayer layer) {
        return entities.stream()
                .filter(e -> e.getCollisionLayer() == layer)
                .toList();
    }

    /**
     * Testa tutte le coppie tra listA e listB.
     * Se sono la stessa lista (Missile×Missile) usa i < j per evitare duplicati.
     */
    private void checkPairs(final List<ICollidable> listA,
                            final List<ICollidable> listB,
                            final InternalEvent eventType) {

        final boolean sameLists = listA == listB;

        for (int i = 0; i < listA.size(); i++) {
            final int start = sameLists ? i + 1 : 0;

            for (int j = start; j < listB.size(); j++) {
                final ICollidable a = listA.get(i);
                final ICollidable b = listB.get(j);
                if (a == b) continue;

                final Hitbox ha = a.getHitbox();
                final Hitbox hb = b.getHitbox();

                if (ha.intersects(hb)) {
                    final Vector2 point = ha.collisionPoint(hb);
                   
                    eventListener.onInternalEvent(eventType,
                            new CollisionData(a, b, point));
                }
            }
        }
    }
}