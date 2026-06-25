package outmaneuver.controller.impl;

import java.util.Objects;

import outmaneuver.controller.ScoreController;
import outmaneuver.controller.event.CollisionEvent;
import outmaneuver.controller.event.Event;
import outmaneuver.model.area.entity.collectibles.StarCollectible;
import outmaneuver.model.session.IScoreSession;

public final class ScoreControllerImpl implements ScoreController {

    private final IScoreSession session;
    private long pendingMs;
    private int missilesScore = 20;

    public ScoreControllerImpl(final IScoreSession session) {
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
        switch ((CollisionEvent) evt) {
            case PLANE_COLLECTIBLE_COLLISION -> {
                if (data instanceof StarCollectible star) {
                    session.incrementScore(star.getScoreValue());
                    session.incrementStarsScore(star.getScoreValue());
                }
            }
            case MISSILE_MISSILE_COLLISION -> {
                    session.incrementScore(missilesScore);
                    session.incrementMissilesScore(missilesScore);
            }
            default -> { }
        }
    }
}
