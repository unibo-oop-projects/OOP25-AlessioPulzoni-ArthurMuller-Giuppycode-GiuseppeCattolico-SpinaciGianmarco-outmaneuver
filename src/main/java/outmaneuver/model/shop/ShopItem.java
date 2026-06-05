package outmaneuver.model.shop;

import java.util.Objects;

import outmaneuver.model.area.PlaneStats;

public record ShopItem(PlaneStats stats, int price) {

    public ShopItem {
        Objects.requireNonNull(stats, "stats must not be null");
        if (price < 0) {
            throw new IllegalArgumentException("price must not be negative");
        }
    }
}
