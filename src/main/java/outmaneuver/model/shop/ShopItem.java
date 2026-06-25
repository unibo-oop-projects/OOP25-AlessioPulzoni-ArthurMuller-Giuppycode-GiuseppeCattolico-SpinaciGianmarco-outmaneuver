package outmaneuver.model.shop;

import java.util.Objects;

import outmaneuver.model.area.entity.plane.PlaneStats;

/**
 * A purchasable catalog entry: a plane's stats together with its price.
 *
 * @param stats the stats of the plane being sold
 * @param price the price in coins
 */
public record ShopItem(PlaneStats stats, int price) {

    /** Validates that the item has non-null stats and a non-negative price. */
    public ShopItem {
        Objects.requireNonNull(stats, "stats must not be null");
        if (price < 0) {
            throw new IllegalArgumentException("price must not be negative");
        }
    }
}
