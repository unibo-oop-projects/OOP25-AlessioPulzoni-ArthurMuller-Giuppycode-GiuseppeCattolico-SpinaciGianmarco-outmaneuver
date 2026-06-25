package outmaneuver.model.session;

public interface IScoreSession {

    int getScore();

    int getStarsScore();

    int getMissilesScore();

    /** Azzera score e contatori **/
    void reset();

    /**
     * Incrementa il punteggio del valore indicato.
     *
     * @param delta valore positivo da aggiungere
     * @throws IllegalArgumentException se delta non è positivo
     */
    void incrementScore(int delta);

    void incrementStarsScore(int delta);

    void incrementMissilesScore(int delta);

}
