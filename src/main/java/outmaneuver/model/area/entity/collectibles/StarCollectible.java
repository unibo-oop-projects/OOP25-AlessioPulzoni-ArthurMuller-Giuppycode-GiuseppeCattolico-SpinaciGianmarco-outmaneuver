package outmaneuver.model.area.entity.collectibles;

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

    public int getScoreValue() {
        return scoreValue;
    }
}
