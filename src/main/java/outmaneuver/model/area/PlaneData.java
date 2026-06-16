package outmaneuver.model.area;

import java.util.Objects;

public record PlaneData(
        String id,
        double baseSpeed,
        double turnRate,
        double hitboxRadius,
        String spriteId,
        int price
) implements PlaneStats {

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
