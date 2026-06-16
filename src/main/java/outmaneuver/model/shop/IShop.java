package outmaneuver.model.shop;

import java.util.List;

import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.wallet.IWallet;

public interface IShop {

    /** Restituisce il catalogo completo degli articoli disponibili. */
    List<ShopItem> getCatalog();

    /**
     * Acquista {@code item}: se il saldo è sufficiente scala il prezzo
     * dal wallet e aggiorna le stats del velivolo.
     *
     * @return {@code true} se l'acquisto è andato a buon fine,
     *         {@code false} se il saldo è insufficiente
     * @throws IllegalArgumentException se {@code item} non appartiene al catalogo
     */
    boolean purchase(ShopItem item, Plane plane, IWallet wallet);
}
