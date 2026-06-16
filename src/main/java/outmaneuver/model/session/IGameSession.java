package outmaneuver.model.session;

public interface IGameSession {

    GameState getGameState();

    int getScore();

    /** Restituisce i millisecondi trascorsi in stato PLAYING. */
    long getElapsedTimeMillis();

    /** Azzera score, stato e timer (chiamato ad ogni nuova partita). */
    void reset();

    /**
     * Incrementa il punteggio del valore indicato.
     *
     * @param delta valore positivo da aggiungere
     * @throws IllegalArgumentException se delta non è positivo
     */
    void incrementScore(int delta);

    /**
     * Transita allo stato indicato.
     *
     * @param state nuovo stato
     * @throws IllegalArgumentException se la transizione non è ammessa
     */
    void transitionTo(GameState state);
}
