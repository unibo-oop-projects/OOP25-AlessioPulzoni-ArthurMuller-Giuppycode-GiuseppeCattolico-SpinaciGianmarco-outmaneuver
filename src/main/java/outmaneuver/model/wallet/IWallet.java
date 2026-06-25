package outmaneuver.model.wallet;

/** A coin balance that can be spent and topped up. */
public interface IWallet {

    /**
     * Returns the current coin balance.
     *
     * @return the number of coins currently owned
     */
    int getCoins();

    /**
     * Aggiunge monete al saldo e persiste.
     *
     * @param amount il numero di monete da aggiungere, deve essere positivo
     * @throws IllegalArgumentException se amount non è positivo
     */
    void addCoins(int amount);

    /**
     * Scala {@code amount} monete dal saldo e persiste.
     *
     * @param amount il numero di monete da scalare, deve essere positivo
     * @return {@code true} se il saldo era sufficiente e la spesa è avvenuta,
     *         {@code false} se saldo insufficiente (saldo invariato)
     * @throws IllegalArgumentException se amount non è positivo
     */
    boolean spend(int amount);
}
