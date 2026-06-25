package outmaneuver.model.area.entity.plane;

import java.util.Objects;

/**
 * Immutable definition of a plane type, loaded from JSON, describing its movement
 * characteristics, appearance and shop price.
 *
 * @param id the plane type identifier
 * @param baseSpeed the plane's base movement speed, in world units per second
 * @param turnRate the plane's turning speed, in radians per second
 * @param hitboxRadius the hitbox radius
 * @param spriteId the identifier of the sprite used to render this plane
 * @param price the cost, in coins, to purchase this plane in the shop
 */
public record PlaneData(
        String id,
        double baseSpeed,
        double turnRate,
        double hitboxRadius,
        String spriteId,
        int price
) implements PlaneStats {

    /**
     * Validates the plane definition's invariants.
     */
    public PlaneData {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(spriteId, "spriteId must not be null");
        if (baseSpeed <= 0) {
            throw new IllegalArgumentException("baseSpeed must be positive");
        }
        if (turnRate <= 0) {
            throw new IllegalArgumentException("turnRate must be positive");
        }
        if (hitboxRadius <= 0) {
            throw new IllegalArgumentException("hitboxRadius must be positive");
        }
        if (price < 0) {
            throw new IllegalArgumentException("price must not be negative");
        }
    }

    @Override
    public String getId() {
        return id();
    }

    @Override
    public double getBaseSpeed() {
        return baseSpeed();
    }

    @Override
    public double getTurnRate() {
        return turnRate();
    }

    @Override
    public double getHitboxRadius() {
        return hitboxRadius();
    }

    @Override
    public String getSpriteId() {
        return spriteId();
    }
}
