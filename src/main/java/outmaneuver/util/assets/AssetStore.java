package outmaneuver.util.assets;

import java.awt.image.BufferedImage;

@FunctionalInterface
public interface AssetStore {

    BufferedImage getSprite(SpriteId id);
}
