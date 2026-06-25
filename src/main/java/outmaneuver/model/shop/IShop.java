package outmaneuver.model.shop;

import java.util.List;

import outmaneuver.model.wallet.IWallet;

/** Catalog of purchasable plane upgrades, with purchase logic against a wallet. */
public interface IShop {

    /**
     * Restituisce il catalogo completo degli articoli disponibili.
     *
     * @return la lista degli articoli in catalogo
     */
    List<ShopItem> getCatalog();

    /**
     * Acquista {@code item}: se il saldo è sufficiente scala il prezzo
     * dal wallet.
     *
     * @param item l'articolo da acquistare
     * @param wallet il wallet da cui scalare il prezzo
     * @return {@code true} se l'acquisto è andato a buon fine,
     *         {@code false} se il saldo è insufficiente
     * @throws IllegalArgumentException se {@code item} non appartiene al catalogo
     */
    boolean purchase(ShopItem item, IWallet wallet);
}
