package outmaneuver.model.area.entity.collectibles;

import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.session.IGameSession;

public final class StarCollectible implements Collectible {

    private final int scoreValue;

    public StarCollectible(final int scoreValue) {
        if (scoreValue <= 0) {
            throw new IllegalArgumentException("scoreValue must be positive");
        }
        this.scoreValue = scoreValue;
    }

    @Override
    public void apply(final Plane plane, final IGameSession session) {
        session.incrementScore(scoreValue);
    }
}
