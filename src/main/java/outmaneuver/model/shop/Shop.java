package outmaneuver.model.shop;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import outmaneuver.model.wallet.IWallet;

public final class Shop implements IShop {

    private final List<ShopItem> catalog;

    public Shop(final List<ShopItem> catalog) {
        Objects.requireNonNull(catalog, "catalog must not be null");
        if (catalog.isEmpty()) {
            throw new IllegalArgumentException("catalog must not be empty");
        }
        this.catalog = Collections.unmodifiableList(List.copyOf(catalog));
    }

    @Override
    public List<ShopItem> getCatalog() {
        return catalog;
    }

    @Override
    public boolean purchase(final ShopItem item, final IWallet wallet) {
        Objects.requireNonNull(item, "item must not be null");
        Objects.requireNonNull(wallet, "wallet must not be null");
        if (!catalog.contains(item)) {
            throw new IllegalArgumentException("item not in catalog: " + item);
        }
        return wallet.spend(item.price());
    }
}
