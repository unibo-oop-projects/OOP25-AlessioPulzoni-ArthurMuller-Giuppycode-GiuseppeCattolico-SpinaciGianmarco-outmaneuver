package outmaneuver.controller;

import outmaneuver.controller.event.InternalEventListener;

public interface ScoreController extends InternalEventListener {

    /**
     * Chiamato ad ogni tick del game loop.
     *
     * @param deltaMs millisecondi trascorsi dall'ultimo tick
     */
    void onTick(long deltaMs);

    /** Azzera lo stato interno (chiamato ad ogni nuova partita). */
    void reset();
}
