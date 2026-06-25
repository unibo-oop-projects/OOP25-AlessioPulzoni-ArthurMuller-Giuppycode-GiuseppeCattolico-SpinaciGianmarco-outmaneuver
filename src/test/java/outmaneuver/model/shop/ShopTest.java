package outmaneuver.model.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.model.area.entity.plane.PlaneData;
import outmaneuver.model.area.entity.plane.PlaneRepository;
import outmaneuver.model.wallet.IWallet;

class ShopTest {

    private static final PlaneData PLANE_A = new PlaneData("a", 10, 5, 3, "sprite_a", 200);
    private static final PlaneData PLANE_B = new PlaneData("b", 12, 6, 4, "sprite_b", 100);

    private ShopItem item;
    private IShop shop;

    @BeforeEach
    void setUp() {
        final PlaneRepository repo = mock(PlaneRepository.class);
        when(repo.loadAll()).thenReturn(List.of(PLANE_A));
        shop = new Shop(repo);
        item = shop.getCatalog().getFirst();
    }

    @Test
    void catalogContainsInsertedItem() {
        assertEquals(List.of(item), shop.getCatalog());
    }

    @Test
    void catalogIsUnmodifiable() {
        assertThrows(UnsupportedOperationException.class, () -> shop.getCatalog().add(item));
    }

    @Test
    void purchaseSucceedsWhenWalletHasEnoughCoins() {
        final IWallet wallet = mock(IWallet.class);
        when(wallet.spend(200)).thenReturn(true);

        assertTrue(shop.purchase(item, wallet));
    }

    @Test
    void purchaseFailsWhenWalletHasInsufficientCoins() {
        final IWallet wallet = mock(IWallet.class);
        when(wallet.spend(200)).thenReturn(false);

        assertFalse(shop.purchase(item, wallet));
    }

    @Test
    void purchaseThrowsForItemNotInCatalog() {
        final ShopItem unknown = new ShopItem(PLANE_B, 100);
        final IWallet wallet   = mock(IWallet.class);

        assertThrows(IllegalArgumentException.class, () -> shop.purchase(unknown, wallet));
    }

    @Test
    void constructorRejectsEmptyCatalog() {
        final PlaneRepository emptyRepo = mock(PlaneRepository.class);
        when(emptyRepo.loadAll()).thenReturn(List.of());
        assertThrows(IllegalArgumentException.class, () -> new Shop(emptyRepo));
    }

    @Test
    void constructorRejectsNullRepo() {
        assertThrows(NullPointerException.class, () -> new Shop(null));
    }
}
