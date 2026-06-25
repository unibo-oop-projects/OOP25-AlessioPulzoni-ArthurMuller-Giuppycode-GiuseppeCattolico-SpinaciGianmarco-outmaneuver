package outmaneuver.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import outmaneuver.controller.event.CollisionEvent;
import outmaneuver.controller.event.InternalEventListener;
import outmaneuver.model.area.collision.CollisionData;
import outmaneuver.model.area.collision.CollisionLayer;
import outmaneuver.model.area.collision.Hitbox;
import outmaneuver.model.area.collision.ICollidable;
import outmaneuver.util.Vector2;

/**
 * Detects collisions between registered entities by layer (missile, plane, collectible)
 * and reports each hit to an {@link InternalEventListener} as a {@link CollisionEvent}
 * carrying a {@link CollisionData} payload.
 */
public final class CollisionEngine {

     private final InternalEventListener eventListener;

    /** Entità attive nella scena corrente. */
    private final List<ICollidable> entities = new ArrayList<>();

    /**
     * Creates a collision engine that reports detected collisions to the given listener.
     *
     * @param eventListener the listener notified of each detected collision
     */
    public CollisionEngine(final InternalEventListener eventListener) {
        this.eventListener = Objects.requireNonNull(eventListener,
                "eventListener must not be null");
    }

    // Registrazione entità (chiamato da EntityController / GameScene)

    /**
     * Registers an entity so it participates in collision checks on subsequent {@link #tick()} calls.
     *
     * @param entity the entity to register
     */
    public void register(final ICollidable entity) {
        entities.add(Objects.requireNonNull(entity));
    }

    /**
     * Removes an entity from collision checking.
     *
     * @param entity the entity to unregister
     */
    public void unregister(final ICollidable entity) {
        entities.remove(entity);
    }

    /**
     * Itera su tutte le coppie rilevanti e verifica l'intersezione delle hitbox.
     * Quando rileva un hit chiama {@code eventListener.onInternalEvent()} con
     * l'evento appropriato e un {@link CollisionData} come payload.
     */
    public void tick() {
        final List<ICollidable> missiles = filterByLayer(CollisionLayer.MISSILE);
        final List<ICollidable> planes = filterByLayer(CollisionLayer.PLANE);
        final List<ICollidable> collectibles = filterByLayer(CollisionLayer.COLLECTIBLE);

        // Missile × Missile
        checkPairs(missiles, missiles, CollisionEvent.MISSILE_MISSILE_COLLISION);

        // Missile × Plane

        checkPairs(missiles, planes, CollisionEvent.PLANE_MISSILE_COLLISION);

        // Plane × Collectible
        checkPairs(planes, collectibles, CollisionEvent.PLANE_COLLECTIBLE_COLLISION);
    }

    private List<ICollidable> filterByLayer(final CollisionLayer layer) {
        return entities.stream()
                .filter(e -> e.getCollisionLayer() == layer)
                .toList();
    }

    /**
     * Testa tutte le coppie tra listA e listB.
     * Se sono la stessa lista (Missile×Missile) usa i < j per evitare duplicati.
     *
     * @param listA the first list of candidates
     * @param listB the second list of candidates
     * @param collisionType the event to report for each detected hit
     */
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private void checkPairs(final List<ICollidable> listA,
                            final List<ICollidable> listB,
                            final CollisionEvent collisionType) {

        final boolean sameLists = listA == listB;

        for (int i = 0; i < listA.size(); i++) {
            final int start = sameLists ? i + 1 : 0;

            for (int j = start; j < listB.size(); j++) {
                final ICollidable a = listA.get(i);
                final ICollidable b = listB.get(j);
                if (a == b) {
                    continue;
                }

                final Hitbox ha = a.getHitbox();
                final Hitbox hb = b.getHitbox();

                if (ha.intersects(hb)) {
                    final Vector2 point = ha.collisionPoint(hb);

                    eventListener.onInternalEvent(collisionType,
                            new CollisionData(a, b, point));
                }
            }
        }
    }
}
