package outmaneuver.controller.impl;

import java.util.Objects;

import outmaneuver.controller.EntityController;
import outmaneuver.controller.GameEventController;
import outmaneuver.controller.HudController;
import outmaneuver.controller.ScoreController;
import outmaneuver.controller.event.CollisionEvent;
import outmaneuver.controller.event.Event;

import outmaneuver.model.area.collision.CollisionData;
import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.collectibles.Collectible;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.session.IGameSession;

public final class GameEventControllerImpl implements GameEventController {

    private final EntityController primaryEntityController;
    private final HudController hudController;
    private final ScoreController scoreController;
    private final IGameSession session;
    private final Runnable onGameOver;

    public GameEventControllerImpl(
            final EntityController primaryEntityController,
            final HudController hudController,
            final ScoreController scoreController,
            final IGameSession session,
            final Runnable onGameOver) {
        this.primaryEntityController = Objects.requireNonNull(primaryEntityController, "primaryEntityController must not be null");
        this.hudController = Objects.requireNonNull(hudController, "hudController must not be null");
        this.scoreController = scoreController;
        this.session = Objects.requireNonNull(session, "session must not be null");
        this.onGameOver = Objects.requireNonNull(onGameOver, "onGameOver must not be null");
    }

    @Override
    public void onInternalEvent(final Event evt, final Object data) {
        if (!(data instanceof final CollisionData collisionData)) {
            return;
        }

        switch ((CollisionEvent) evt) {
            case PLANE_MISSILE_COLLISION -> {
                primaryEntityController.removeEntity((Entity) collisionData.getEntityA());
                final Plane plane = (Plane) collisionData.getEntityB();
                if (!plane.isShieldActive()) {
                    onGameOver.run();
                }
            }
            case PLANE_COLLECTIBLE_COLLISION -> {
                final Plane plane = (Plane) collisionData.getEntityA();
                final Collectible collectible = (Collectible) collisionData.getEntityB();
                collectible.apply(plane, session);
                primaryEntityController.removeEntity(collectible);
                hudController.onInternalEvent(CollisionEvent.PLANE_COLLECTIBLE_COLLISION, collectible);
                if (scoreController != null) {
                    scoreController.onInternalEvent(CollisionEvent.PLANE_COLLECTIBLE_COLLISION, collectible);
                }
            }
            case MISSILE_MISSILE_COLLISION -> {
                primaryEntityController.removeEntity((Entity) collisionData.getEntityA());
                primaryEntityController.removeEntity((Entity) collisionData.getEntityB());
                if (scoreController != null) {
                    scoreController.onInternalEvent(CollisionEvent.MISSILE_MISSILE_COLLISION, collisionData);
                }
            }
        }
    }
}
