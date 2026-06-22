package outmaneuver.controller.impl;

import java.util.Objects;

import outmaneuver.controller.EntityController;
import outmaneuver.controller.GameEventController;
import outmaneuver.controller.HudController;
import outmaneuver.controller.CollisionEvent;
import outmaneuver.controller.ScoreController;

import outmaneuver.model.area.collision.CollisionData;
import outmaneuver.model.area.entity.collectibles.Collectible;
import outmaneuver.model.area.entity.plane.Plane;

public final class GameEventControllerImpl implements GameEventController {

    private final EntityController primaryEntityController;
    private final HudController hudController;
    private final ScoreController scoreController;
    private final Runnable onGameOver;

    public GameEventControllerImpl(
            final EntityController primaryEntityController,
            final HudController hudController,
            final ScoreController scoreController,
            final Runnable onGameOver) {
        this.primaryEntityController = Objects.requireNonNull(primaryEntityController, "primaryEntityController must not be null");
        this.hudController = Objects.requireNonNull(hudController, "hudController must not be null");
        this.scoreController = scoreController;
        this.onGameOver = Objects.requireNonNull(onGameOver, "onGameOver must not be null");
    }

    @Override
    public void onInternalEvent(final CollisionEvent evt, final Object data) {
        if (!(data instanceof final CollisionData collisionData)) {
            return;
        }
        primaryEntityController.onInternalEvent(evt, collisionData);

        switch (evt) {
            case PLANE_MISSILE_COLLISION -> {
                final Plane plane = (Plane) collisionData.getEntityB();
                if (!plane.isShieldActive()) {
                    onGameOver.run();
                }
            }
            case PLANE_COLLECTIBLE_COLLISION -> {
                if (collisionData.getEntityB() instanceof final Collectible collectible) {
                    hudController.onInternalEvent(CollisionEvent.PLANE_COLLECTIBLE_COLLISION, collectible);
                    if (scoreController != null) {
                        scoreController.onInternalEvent(CollisionEvent.PLANE_COLLECTIBLE_COLLISION, collectible);
                    }
                }
            }
            case MISSILE_MISSILE_COLLISION -> {
                if (scoreController != null) {
                    scoreController.onInternalEvent(CollisionEvent.MISSILE_MISSILE_COLLISION, collisionData);
                }
            }
        }
    }
}
