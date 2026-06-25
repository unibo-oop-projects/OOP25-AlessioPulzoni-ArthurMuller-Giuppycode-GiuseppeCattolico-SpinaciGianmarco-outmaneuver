package outmaneuver.util.assets;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

/**
 * {@link AssetStore} that eagerly loads every sprite from the classpath
 * (under {@code /assets/sprites/}) and caches it in memory. Missing or
 * unreadable sprites fall back to a generated placeholder image.
 */
public final class ClasspathAssetStore implements AssetStore {

    private static final String BASE_PATH = "/assets/sprites/";
    private static final int PLACEHOLDER_SIZE = 64;
    private static final int PLACEHOLDER_LABEL_BASELINE = 14;

    private final Map<SpriteId, BufferedImage> cache;

    /** Eagerly loads (or generates a placeholder for) every {@link SpriteId}. */
    public ClasspathAssetStore() {
        final Map<SpriteId, BufferedImage> map = new EnumMap<>(SpriteId.class);
        for (final var id : SpriteId.values()) {
            map.put(id, load(id));
        }
        this.cache = Map.copyOf(map);
    }

    @Override
    public BufferedImage getSprite(final SpriteId id) {
        return cache.get(id);
    }

    @SuppressWarnings("PMD.SystemPrintln")
    private static BufferedImage load(final SpriteId id) {
        final String path = BASE_PATH + id.getFilename() + ".png";
        try (InputStream in = ClasspathAssetStore.class.getResourceAsStream(path)) {
            if (in == null) {
                System.err.println("[AssetStore] MISSING: " + path);
                return placeholder(id);
            }
            return ImageIO.read(in);
        } catch (final IOException e) {
            System.err.println("[AssetStore] FAILED to read: " + path + " - " + e.getMessage());
            return placeholder(id);
        }
    }

    private static BufferedImage placeholder(final SpriteId id) {
        final BufferedImage img = new BufferedImage(
                PLACEHOLDER_SIZE, PLACEHOLDER_SIZE, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = img.createGraphics();
        g.setColor(Color.MAGENTA);
        g.fillRect(0, 0, PLACEHOLDER_SIZE, PLACEHOLDER_SIZE);
        g.setColor(Color.BLACK);
        g.drawString(id.name(), 2, PLACEHOLDER_LABEL_BASELINE);
        g.dispose();
        return img;
    }
}
