package outmaneuver.controller.event;

import java.util.Objects;

import outmaneuver.controller.ScoreController;
import outmaneuver.controller.impl.CollectibleControllerImpl;
import outmaneuver.controller.impl.MasterControllerImpl;
import outmaneuver.controller.impl.SessionState;
import outmaneuver.controller.impl.PlaneControllerImpl;
import outmaneuver.controller.impl.missile.MissileControllerImpl;
import outmaneuver.model.area.collision.CollisionData;
import outmaneuver.model.area.effect.Effect;
import outmaneuver.model.area.effect.EffectType;
import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.collectibles.Collectible;
import outmaneuver.model.area.entity.collectibles.StarCollectible;
import outmaneuver.model.area.entity.missile.Missile; //AGGIUNTO: serve per far reagire i missili (onCollision)

public final class EventController implements InternalEventListener {

    private final SessionState session;
    private final PlaneControllerImpl planeController;
    private final CollectibleControllerImpl collectibleController;
    private final MissileControllerImpl missileController;
    private final ScoreController scoreController;
    private final Runnable onGameOver;

    public EventController(
            final MasterControllerImpl master,
            final SessionState session,
            final ScoreController scoreController,
            final Runnable onGameOver) {
        this.session = Objects.requireNonNull(session);
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
                if (session.isShieldActive()) { //AGGIUNTO: aereo scudato -> niente game over, ma il missile reagisce e viene comunque distrutto
                    final Missile missile = (Missile) collisionData.getEntityA(); //AGGIUNTO: il missile coinvolto
                    missile.onCollision(missileController.activeMissiles()); //AGGIUNTO: fa scattare la reazione (es. il clock rallenta gli altri missili)
                    missileController.removeEntity((Entity) missile); //AGGIUNTO: il missile e' SEMPRE distrutto contro l'aereo, anche lo shield missile col suo scudo (il "regge due colpi" vale solo tra missili)
                } else { //AGGIUNTO: senza lo scudo dell'aereo l'impatto e' fatale
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
                    session.increaseStars();
                }
            }
            case MISSILE_MISSILE_COLLISION -> {
                final Missile a = (Missile) collisionData.getEntityA(); //AGGIUNTO: i due missili coinvolti nello scontro
                final Missile b = (Missile) collisionData.getEntityB(); //AGGIUNTO
                final var active = missileController.activeMissiles(); //AGGIUNTO: lista dei missili attivi, serve alla reazione (es. il clock rallenta questi)
                a.onCollision(active); //AGGIUNTO: reazione del primo (shield regge / clock rallenta / normale si distrugge)
                b.onCollision(active); //AGGIUNTO: reazione del secondo
                if (!a.isAlive()) { //AGGIUNTO: rimuovo il primo solo se la reazione lo ha distrutto (lo shield regge il 1o colpo)
                    missileController.removeEntity((Entity) a); //AGGIUNTO
                }
                if (!b.isAlive()) { //AGGIUNTO: rimuovo il secondo solo se distrutto
                    missileController.removeEntity((Entity) b); //AGGIUNTO
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
                    session.setShieldActive(true);
                    missileController.setShieldActrive(true);
                }
                if (effect.getType() == EffectType.SPEED_BOOST) {
                    planeController.setSpeedMultiplier(effect.getMultiplier());
                    missileController.setSpeedMultiplier(effect.getMultiplier());
                    session.setSpeedMultiplier(effect.getMultiplier());
                }
            }
            case EFFECT_EXPIRED -> {
                if (effect.getType() == EffectType.SHIELD) {
                    session.setShieldActive(false);
                    missileController.setShieldActrive(false);
                }
                if (effect.getType() == EffectType.SPEED_BOOST) {
                    planeController.setSpeedMultiplier(1.0);
                    missileController.setSpeedMultiplier(1.0);
                    session.setSpeedMultiplier(1.0);
                }
            }
        }
    }

}
