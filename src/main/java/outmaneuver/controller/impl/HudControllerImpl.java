package outmaneuver.controller.impl;

import outmaneuver.controller.HudController;
import outmaneuver.controller.event.CollisionEvent;
import outmaneuver.controller.event.Event;
import outmaneuver.model.area.collision.CollisionData;
import outmaneuver.model.area.entity.collectibles.StarCollectible;

public final class HudControllerImpl implements HudController {

    private long elapsedMs;
    private double speedMultiplier = 1.0;
    private boolean shieldActive;
    private int stars;

    @Override
    public void onTick(final long deltaMs) {
        elapsedMs += deltaMs;
    }

    @Override
    public void setShieldActive(final boolean active) {
        this.shieldActive = active;
    }

    @Override
    public void setSpeedMultiplier(final double multiplier) {
        this.speedMultiplier = multiplier;
    }

    @Override
    public long getElapsedMs() {
        return elapsedMs;
    }

    @Override
    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    @Override
    public boolean isShieldActive() {
        return shieldActive;
    }

    @Override
    public int getStars() {
        return stars;
    }

    @Override
    public void reset() {
        this.elapsedMs = 0L;
        this.speedMultiplier = 1.0;
        this.shieldActive = false;
        this.stars = 0;
    }

    @Override
    public void onInternalEvent(final Event evt, final Object data) {
        if (evt == CollisionEvent.PLANE_COLLECTIBLE_COLLISION
                && data instanceof final CollisionData cd
                && cd.getEntityB() instanceof StarCollectible) {
            stars++;
        }
    }
}
