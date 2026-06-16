package outmaneuver.model.area.entity.collectibles;

import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.session.IGameSession;
import outmaneuver.util.Vector2;

public final class StarCollectible extends AbstractCollectible {

    private final int scoreValue;

    public StarCollectible(final Vector2 position, final int scoreValue) {
        super(position);
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
