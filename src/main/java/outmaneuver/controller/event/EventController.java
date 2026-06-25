package outmaneuver.controller.event;

import java.util.Objects;

import outmaneuver.controller.ScoreController;
import outmaneuver.controller.impl.CollectibleControllerImpl;
import outmaneuver.controller.impl.MasterControllerImpl;
import outmaneuver.controller.impl.PlaneControllerImpl;
import outmaneuver.controller.impl.missile.MissileControllerImpl;
import outmaneuver.model.area.collision.CollisionData;
import outmaneuver.model.area.effect.Effect;
import outmaneuver.model.area.effect.EffectType;
import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.collectibles.Collectible;
import outmaneuver.model.area.entity.collectibles.StarCollectible;

public final class EventController implements InternalEventListener {

    private final PlaneControllerImpl planeController;
    private final CollectibleControllerImpl collectibleController;
    private final MissileControllerImpl missileController;
    private final ScoreController scoreController;
    private final Runnable onGameOver;
    private boolean shieldActive;
    private double speedMultiplier = 1.0;
    private int stars;

    public EventController(
            final MasterControllerImpl master,
            final ScoreController scoreController,
            final Runnable onGameOver) {
        this.planeController = master.getEntityController(PlaneControllerImpl.class).orElseThrow();
        this.collectibleController = master.getEntityController(CollectibleControllerImpl.class).orElseThrow();
        this.missileController = master.getEntityController(MissileControllerImpl.class).orElseThrow();
        this.scoreController = scoreController;
        this.onGameOver = Objects.requireNonNull(onGameOver, "onGameOver must not be null");
    }

    @Override
    public void onInternalEvent(final Event evt, final Object data) {
        if (evt instanceof EffectEvent) {
            handleEffectEvent((EffectEvent) evt, data);
            return;
        }

        if (!(data instanceof final CollisionData collisionData)) {
            return;
        }

        switch ((CollisionEvent) evt) {
            case PLANE_MISSILE_COLLISION -> {
                planeController.removeEntity((Entity) collisionData.getEntityA());
                if (!shieldActive) {
                    onGameOver.run();
                }
            }
            case PLANE_COLLECTIBLE_COLLISION -> {
                final var collectible = (Collectible) collisionData.getEntityB();
                if (collectible.getEffect() != null) {
                    collectibleController.addEffect(collectible.getEffect());
                }
                collectibleController.removeEntity(collectible);
                if (scoreController != null) {
                    scoreController.onInternalEvent(CollisionEvent.PLANE_COLLECTIBLE_COLLISION, collectible);
                }
                if (collectible instanceof StarCollectible) {
                    stars++;
                }
            }
            case MISSILE_MISSILE_COLLISION -> {
                missileController.removeEntity((Entity) collisionData.getEntityA());
                missileController.removeEntity((Entity) collisionData.getEntityB());
                if (scoreController != null) {
                    scoreController.onInternalEvent(CollisionEvent.MISSILE_MISSILE_COLLISION, collisionData);
                }
            }
        }
    }

    private void handleEffectEvent(final EffectEvent evt, final Object data) {
        final var effect = (Effect) data;
        switch (evt) {
            case EFFECT_APPLIED -> {
                if (effect.getType() == EffectType.SHIELD) {
                    shieldActive = true;
                    missileController.setShieldActrive(true);
                }
                if (effect.getType() == EffectType.SPEED_BOOST) {
                    planeController.setSpeedMultiplier(effect.getMultiplier());
                    missileController.setSpeedMultiplier(effect.getMultiplier());
                    this.speedMultiplier = effect.getMultiplier();
                }
            }
            case EFFECT_EXPIRED -> {
                if (effect.getType() == EffectType.SHIELD) {
                    shieldActive = false;
                    missileController.setShieldActrive(false);
                }
                if (effect.getType() == EffectType.SPEED_BOOST) {
                    planeController.setSpeedMultiplier(1.0);
                    missileController.setSpeedMultiplier(1.0);
                    this.speedMultiplier = 1.0;
                }
            }
        }
    }

    public int getStars() {
        return stars;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public boolean isShieldActive() {
        return shieldActive;
    }

    public void reset() {
        this.stars = 0;
        this.speedMultiplier = 1.0;
        this.shieldActive = false;
    }
}
