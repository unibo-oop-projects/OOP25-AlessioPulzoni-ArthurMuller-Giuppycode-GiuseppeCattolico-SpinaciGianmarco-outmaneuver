package outmaneuver.util.assets;

import java.awt.image.BufferedImage;

/** Provides the sprite images used to render game entities. */
@FunctionalInterface
public interface AssetStore {

    /**
     * Returns the sprite image for the given asset id.
     *
     * @param id the sprite to look up
     * @return the corresponding sprite image
     */
    BufferedImage getSprite(SpriteId id);
}
