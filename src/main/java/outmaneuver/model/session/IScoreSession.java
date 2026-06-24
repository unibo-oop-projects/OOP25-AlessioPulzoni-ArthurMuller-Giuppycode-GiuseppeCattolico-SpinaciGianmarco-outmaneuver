package outmaneuver.model.session;

public interface IScoreSession {

    int getScore();

    /** Azzera score **/
    void reset();

    /**
     * Incrementa il punteggio del valore indicato.
     *
     * @param delta valore positivo da aggiungere
     * @throws IllegalArgumentException se delta non è positivo
     */
    void incrementScore(int delta);

}
