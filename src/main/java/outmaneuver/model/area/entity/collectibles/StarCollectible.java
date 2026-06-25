package outmaneuver.model.area.entity.collectibles;

import outmaneuver.util.Vector2;

/** A collectible star that grants score points when picked up, with no timed effect. */
public final class StarCollectible extends AbstractCollectible {

    private final int scoreValue;

    /**
     * Creates a star collectible worth the given score.
     *
     * @param position the spawn position in world coordinates
     * @param scoreValue the score points granted on pickup, must be positive
     */
    public StarCollectible(final Vector2 position, final int scoreValue) {
        super(position);
        if (scoreValue <= 0) {
            throw new IllegalArgumentException("scoreValue must be positive");
        }
        this.scoreValue = scoreValue;
    }

    /**
     * Returns the score points granted by this star.
     *
     * @return the score value
     */
    public int getScoreValue() {
        return scoreValue;
    }

    @Override
    public String getCollectibleType() {
        return "star";
    }
}
