package outmaneuver.model.wallet;

public interface IWallet {

    int getCoins();

    /**
     * Aggiunge monete al saldo e persiste.
     *
     * @throws IllegalArgumentException se amount non è positivo
     */
    void addCoins(int amount);

    /**
     * Scala {@code amount} monete dal saldo e persiste.
     *
     * @return {@code true} se il saldo era sufficiente e la spesa è avvenuta,
     *         {@code false} se saldo insufficiente (saldo invariato)
     * @throws IllegalArgumentException se amount non è positivo
     */
    boolean spend(int amount);
}
