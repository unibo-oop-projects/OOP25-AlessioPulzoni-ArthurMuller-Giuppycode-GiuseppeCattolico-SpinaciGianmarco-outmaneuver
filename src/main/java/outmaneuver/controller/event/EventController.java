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
import outmaneuver.model.area.entity.missile.Missile;

/**
 * Central dispatcher that reacts to collision and effect events by routing the
 * appropriate gameplay consequences (damage, scoring, effect toggling) to the
 * relevant controllers.
 */
public final class EventController implements InternalEventListener {

    private final PlaneControllerImpl planeController;
    private final CollectibleControllerImpl collectibleController;
    private final MissileControllerImpl missileController;
    private final ScoreController scoreController;
    private final Runnable onGameOver;

    /**
     * Creates a dispatcher wired to the plane, collectible and missile controllers
     * registered on the given master controller.
     *
     * @param master the master controller from which entity controllers are looked up
     * @param scoreController the score controller to notify of scoring events
     * @param onGameOver callback invoked when the plane is destroyed without a shield
     */
    public EventController(
            final MasterControllerImpl master,
            final ScoreController scoreController,
            final Runnable onGameOver) {
        this.planeController = master.getEntityController(PlaneControllerImpl.class).orElseThrow();
        this.collectibleController = master.getEntityController(CollectibleControllerImpl.class).orElseThrow();
        this.missileController = master.getEntityController(MissileControllerImpl.class).orElseThrow();
        this.scoreController = Objects.requireNonNull(scoreController, "scoreController must not be null");
        this.onGameOver = Objects.requireNonNull(onGameOver, "onGameOver must not be null");
    }

    @Override
    public void onInternalEvent(final Event evt, final Object data) {
        if (evt instanceof EffectEvent) {
            handleEffectEvent((EffectEvent) evt, data);
            return;
        }

        if (!(data instanceof final CollisionData collisionData) || !(evt instanceof final CollisionEvent collisionEvent)) {
            return;
        }

        switch (collisionEvent) {
            case PLANE_MISSILE_COLLISION -> {
                if (scoreController.isShieldActive()) {
                    final Missile missile = (Missile) collisionData.getEntityA();
                    missile.onCollision(missileController.activeMissiles());
                    missileController.removeEntity((Entity) missile);
                } else {
                    planeController.removeEntity((Entity) collisionData.getEntityA());
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
                    scoreController.increaseStars();
                }
            }
            case MISSILE_MISSILE_COLLISION -> {
                final Missile a = (Missile) collisionData.getEntityA();
                final Missile b = (Missile) collisionData.getEntityB();
                final var active = missileController.activeMissiles();
                a.onCollision(active);
                b.onCollision(active);
                if (!a.isAlive()) {
                    missileController.removeEntity((Entity) a);
                }
                if (!b.isAlive()) {
                    missileController.removeEntity((Entity) b);
                }
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
                    scoreController.setShieldActive(true);
                    missileController.setShieldActive(true);
                }
                if (effect.getType() == EffectType.SPEED_BOOST) {
                    planeController.setSpeedMultiplier(effect.getMultiplier());
                    missileController.setSpeedMultiplier(effect.getMultiplier());
                    scoreController.setSpeedMultiplier(effect.getMultiplier());
                }
            }
            case EFFECT_EXPIRED -> {
                if (effect.getType() == EffectType.SHIELD) {
                    scoreController.setShieldActive(false);
                    missileController.setShieldActive(false);
                }
                if (effect.getType() == EffectType.SPEED_BOOST) {
                    planeController.setSpeedMultiplier(1.0);
                    missileController.setSpeedMultiplier(1.0);
                    scoreController.setSpeedMultiplier(1.0);
                }
            }
        }
    }

}
