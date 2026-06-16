package outmaneuver.model.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.model.area.Plane;
import outmaneuver.model.area.PlaneStats;
import outmaneuver.model.wallet.IWallet;

class ShopTest {

    private PlaneStats stats;
    private ShopItem item;
    private IShop shop;

    @BeforeEach
    void setUp() {
        stats = mock(PlaneStats.class);
        item  = new ShopItem(stats, 200);
        shop  = new Shop(List.of(item));
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
        final Plane plane   = mock(Plane.class);
        final IWallet wallet = mock(IWallet.class);
        when(wallet.spend(200)).thenReturn(true);

        assertTrue(shop.purchase(item, plane, wallet));
        verify(plane).setStats(stats);
    }

    @Test
    void purchaseFailsWhenWalletHasInsufficientCoins() {
        final Plane plane   = mock(Plane.class);
        final IWallet wallet = mock(IWallet.class);
        when(wallet.spend(200)).thenReturn(false);

        assertFalse(shop.purchase(item, plane, wallet));
        verify(plane, never()).setStats(stats);
    }

    @Test
    void purchaseThrowsForItemNotInCatalog() {
        final PlaneStats other = mock(PlaneStats.class);
        final ShopItem unknown = new ShopItem(other, 100);
        final Plane plane      = mock(Plane.class);
        final IWallet wallet    = mock(IWallet.class);

        assertThrows(IllegalArgumentException.class, () -> shop.purchase(unknown, plane, wallet));
    }

    @Test
    void constructorRejectsEmptyCatalog() {
        assertThrows(IllegalArgumentException.class, () -> new Shop(List.of()));
    }

    @Test
    void constructorRejectsNullCatalog() {
        assertThrows(NullPointerException.class, () -> new Shop(null));
    }
}
