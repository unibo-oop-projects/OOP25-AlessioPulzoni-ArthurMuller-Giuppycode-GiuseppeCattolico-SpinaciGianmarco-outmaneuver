package outmaneuver.util.assets;

import java.util.Locale;

/** Identifies every sprite asset used by the game, in lower-case-file-name form. */
public enum SpriteId {

    PLANE_STANDARD,
    PLANE_FAST,
    PLANE_HEAVY,
    MISSILE_BASIC,
    MISSILE_FAST,
    MISSILE_SNIPER,
    MISSILE_BOUNCE,
    MISSILE_SHIELD,
    MISSILE_CLOCK,
    COLLECTIBLE_STAR,
    COLLECTIBLE_SPEED,
    COLLECTIBLE_SHIELD,

    EXPLOSION,

    SHIELD,

    CLOUD_1,
    CLOUD_2,
    CLOUD_3;

    /**
     * Returns the lower-case file name (without extension) for this sprite.
     *
     * @return the file name to look up under the sprites directory
     */
    public String getFilename() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * Resolves a sprite id from its lower-case file name.
     *
     * @param filename the file name to resolve, case-insensitive
     * @return the matching sprite id
     */
    public static SpriteId fromFilename(final String filename) {
        return valueOf(filename.toUpperCase(Locale.ROOT));
    }
}
