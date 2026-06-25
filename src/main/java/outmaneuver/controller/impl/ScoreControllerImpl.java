package outmaneuver.controller.impl;

import java.util.Objects;
import java.util.function.LongSupplier;

import outmaneuver.controller.ScoreController;
import outmaneuver.controller.event.CollisionEvent;
import outmaneuver.controller.event.Event;
import outmaneuver.model.area.entity.collectibles.StarCollectible;
import outmaneuver.model.session.ISession;

/**
 * Default {@link ScoreController} implementation backed by an {@link ISession}: accrues
 * score from elapsed time and from collision events (stars, missile-missile collisions),
 * and tracks the active speed/shield modifiers.
 */
public final class ScoreControllerImpl implements ScoreController {

    private static final String NEGATIVE_DELTA = "delta must be positive, was: ";
    private static final int MISSILES_SCORE = 20;

    private final ISession session;
    private final LongSupplier tickMsSupplier;
    private long pendingMs;

    /**
     * Creates a score controller backed by the given session.
     *
     * @param session the session used to persist score, elapsed time and modifiers
     * @param tickMsSupplier supplies the tick duration used by the no-arg {@link #onTick()}
     */
    public ScoreControllerImpl(final ISession session, final LongSupplier tickMsSupplier) {
        this.session = Objects.requireNonNull(session, "session must not be null");
        this.tickMsSupplier = Objects.requireNonNull(tickMsSupplier, "tickMsSupplier must not be null");
    }

    @Override
    public void onTick() {
        onTick(tickMsSupplier.getAsLong());
    }

    @Override
    public void onTick(final long deltaMs) {
        session.setElapsedMs(session.getElapsedMs() + deltaMs);
        pendingMs += deltaMs;
        final long points = pendingMs / 1_000;
        if (points > 0) {
            addToScore((int) points);
            pendingMs %= 1_000;
        }
    }

    @Override
    public void reset() {
        pendingMs = 0;
        session.reset();
    }

    @Override
    public void onInternalEvent(final Event evt, final Object data) {
        if (!(evt instanceof final CollisionEvent collisionEvent)) {
            return;
        }
        switch (collisionEvent) {
            case PLANE_COLLECTIBLE_COLLISION -> {
                if (data instanceof StarCollectible star) {
                    addToScore(star.getScoreValue());
                    session.setStarsScore(session.getStarsScore() + star.getScoreValue());
                }
            }
            case MISSILE_MISSILE_COLLISION -> {
                addToScore(MISSILES_SCORE);
                session.setMissilesScore(session.getMissilesScore() + MISSILES_SCORE);
            }
            default -> { }
        }
    }

    @Override
    public int getScore() {
        return session.getScore();
    }

    @Override
    public long getElapsedMs() {
        return session.getElapsedMs();
    }

    @Override
    public int getStars() {
        return session.getStars();
    }

    @Override
    public void increaseStars() {
        session.setStars(session.getStars() + 1);
    }

    @Override
    public double getSpeedMultiplier() {
        return session.getSpeedMultiplier();
    }

    @Override
    public void setSpeedMultiplier(final double speedMultiplier) {
        session.setSpeedMultiplier(speedMultiplier);
    }

    @Override
    public boolean isShieldActive() {
        return session.isShieldActive();
    }

    @Override
    public void setShieldActive(final boolean shieldActive) {
        session.setShieldActive(shieldActive);
    }

    private void addToScore(final int delta) {
        if (delta <= 0) {
            throw new IllegalArgumentException(NEGATIVE_DELTA + delta);
        }
        session.setScore(session.getScore() + delta);
    }
}
