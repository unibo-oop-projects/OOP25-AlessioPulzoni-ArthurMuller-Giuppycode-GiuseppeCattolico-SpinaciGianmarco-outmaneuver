package outmaneuver.controller.impl;

import java.util.Objects;

import outmaneuver.controller.CollisionEvent;
import outmaneuver.controller.ScoreController;
import outmaneuver.model.area.entity.collectibles.StarCollectible;
import outmaneuver.model.session.IGameSession;

public final class ScoreControllerImpl implements ScoreController {

    private final IGameSession session;
    private long pendingMs;

    public ScoreControllerImpl(final IGameSession session) {
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
    }

    @Override
    public void onInternalEvent(final CollisionEvent evt, final Object data) {
        switch (evt) {
            case PLANE_COLLECTIBLE_COLLISION -> {
                if (data instanceof StarCollectible star) {
                    session.incrementScore(star.getScoreValue());
                }
            }
            case MISSILE_MISSILE_COLLISION -> {
                    session.incrementScore(20);
            }
            default -> { }
        }
    }
}
