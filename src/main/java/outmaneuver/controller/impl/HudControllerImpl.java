package outmaneuver.controller.impl;

import java.util.Objects;

import outmaneuver.controller.HudController;
import outmaneuver.controller.InternalEvent;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.area.entity.collectibles.StarCollectible;
import outmaneuver.view.HudSnapshot;

public final class HudControllerImpl implements HudController {

    private long accumulatedMs;
    private long lastResumeMs;
    private boolean paused;
    private volatile int stars;

    public HudControllerImpl() {
        reset();
    }

    @Override
    public HudSnapshot buildSnapshot(final Plane plane, final boolean isPaused) {
        Objects.requireNonNull(plane, "plane must not be null");
        final long now = System.currentTimeMillis();
        if (isPaused && !paused) {
            accumulatedMs += now - lastResumeMs;
            paused = true;
        } else if (!isPaused && paused) {
            lastResumeMs = now;
            paused = false;
        }
        final long elapsed = paused ? accumulatedMs : accumulatedMs + (now - lastResumeMs);
        return new HudSnapshot(
                elapsed,
                plane.getEffectiveSpeed(),
                plane.isShieldActive(),
                isPaused,
                stars
        );
    }

    @Override
    public void reset() {
        this.accumulatedMs = 0L;
        this.lastResumeMs = System.currentTimeMillis();
        this.paused = false;
        this.stars = 0;
    }

    @Override
    public void onInternalEvent(final InternalEvent evt, final Object data) {
        if (evt == InternalEvent.PLANE_COLLECTIBLE_COLLISION && data instanceof StarCollectible) {
            stars++;
        }
    }
}
