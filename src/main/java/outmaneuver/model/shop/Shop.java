package outmaneuver.model.shop;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import outmaneuver.model.area.entity.plane.PlaneRepository;
import outmaneuver.model.wallet.IWallet;

/** Default {@link IShop} implementation, with a catalog built from a {@link PlaneRepository}. */
public final class Shop implements IShop {

    private final List<ShopItem> catalog;

    /**
     * Builds the catalog from every plane in the given repository.
     *
     * @param repo the plane repository to build the catalog from
     */
    public Shop(final PlaneRepository repo) {
        Objects.requireNonNull(repo, "repo must not be null");
        final List<ShopItem> items = repo.loadAll().stream()
                .map(p -> new ShopItem(p, p.price()))
                .toList();
        if (items.isEmpty()) {
            throw new IllegalArgumentException("catalog must not be empty");
        }
        this.catalog = Collections.unmodifiableList(items);
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
