package outmaneuver.view.swing.shop;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import outmaneuver.model.area.entity.plane.PlaneData;
import outmaneuver.model.area.entity.plane.PlaneStats;
import outmaneuver.model.shop.ShopItem;

class ShopViewTest {

    private static final PlaneData STATS = new PlaneData("standard", 200, 3, 20, "aircraft_standard", 0);
    private static final ShopItem ITEM = new ShopItem(STATS, 100);
    private static final Supplier<PlaneStats> EQUIPPED = () -> STATS;
    private static final Predicate<String> NOT_OWNED  = id -> false;

    private ShopView build() {
        return new ShopView(List.of(ITEM), () -> 500, EQUIPPED, NOT_OWNED, item -> true, () -> { });
    }

    @Test
    void constructorDoesNotThrowWithValidArgs() {
        assertDoesNotThrow(this::build);
    }

    @Test
    void constructorRejectsNullCatalog() {
        assertThrows(NullPointerException.class, () -> new ShopView(
                null, () -> 0, EQUIPPED, NOT_OWNED, item -> true, () -> { }));
    }

    @Test
    void constructorRejectsEmptyCatalog() {
        assertThrows(IllegalArgumentException.class, () -> new ShopView(
                List.of(), () -> 0, EQUIPPED, NOT_OWNED, item -> true, () -> { }));
    }

    @Test
    void constructorRejectsNullCoinsSupplier() {
        assertThrows(NullPointerException.class, () -> new ShopView(
                List.of(ITEM), null, EQUIPPED, NOT_OWNED, item -> true, () -> { }));
    }

    @Test
    void constructorRejectsNullEquippedSupplier() {
        assertThrows(NullPointerException.class, () -> new ShopView(
                List.of(ITEM), () -> 0, null, NOT_OWNED, item -> true, () -> { }));
    }

    @Test
    void constructorRejectsNullIsOwned() {
        assertThrows(NullPointerException.class, () -> new ShopView(
                List.of(ITEM), () -> 0, EQUIPPED, null, item -> true, () -> { }));
    }

    @Test
    void constructorRejectsNullOnPurchase() {
        assertThrows(NullPointerException.class, () -> new ShopView(
                List.of(ITEM), () -> 0, EQUIPPED, NOT_OWNED, null, () -> { }));
    }

    @Test
    void constructorRejectsNullOnBack() {
        assertThrows(NullPointerException.class, () -> new ShopView(
                List.of(ITEM), () -> 0, EQUIPPED, NOT_OWNED, item -> true, null));
    }

    @Test
    void refreshCoinsDoesNotThrow() {
        assertDoesNotThrow(() -> build().refreshCoins());
    }
}
