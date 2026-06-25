package outmaneuver.controller.impl;

import java.util.Objects;

import outmaneuver.controller.ScoreController;
import outmaneuver.controller.event.CollisionEvent;
import outmaneuver.controller.event.Event;
import outmaneuver.model.area.entity.collectibles.StarCollectible;
import outmaneuver.model.session.ISession;

public final class ScoreControllerImpl implements ScoreController {

    private static final int MISSILES_SCORE = 20;

    private final ISession session;
    private long pendingMs;

    public ScoreControllerImpl(final ISession session) {
        this.session = Objects.requireNonNull(session, "session must not be null");
    }

    @Override
    public void onTick(final long deltaMs) {
        pendingMs += deltaMs;
        final long points = pendingMs / 1_000;
        if (points > 0) {
            session.incrementScore((int) points);
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
                    session.incrementScore(star.getScoreValue());
                    session.incrementStarsScore(star.getScoreValue());
                }
            }
            case MISSILE_MISSILE_COLLISION -> {
                    session.incrementScore(MISSILES_SCORE);
                    session.incrementMissilesScore(MISSILES_SCORE);
            }
            default -> { }
        }
    }
}
