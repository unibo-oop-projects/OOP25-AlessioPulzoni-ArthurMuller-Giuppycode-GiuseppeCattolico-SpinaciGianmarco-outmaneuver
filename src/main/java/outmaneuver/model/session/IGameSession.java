package outmaneuver.model.session;

import outmaneuver.model.area.Plane;

public interface IGameSession {

    GameState getGameState();

    int getScore();

    /** Restituisce i millisecondi trascorsi in stato PLAYING. */
    long getElapsedTimeMillis();

    void equipPlane(Plane plane);

    /**
     * Incrementa il punteggio del valore indicato.
     *
     * @param delta valore positivo da aggiungere
     * @throws IllegalArgumentException se delta &lt;= 0
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
