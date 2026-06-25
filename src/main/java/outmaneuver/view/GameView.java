package outmaneuver.view;

/**
 * Abstraction for a renderable game surface, decoupling the controllers from
 * the concrete UI toolkit (e.g. Swing) used to display the game.
 */
public interface GameView {

    /**
     * Renders a new frame using the given snapshot of the game's state.
     *
     * @param state the render state to draw
     */
    void renderFrame(RenderState state);

    /**
     * Returns the current width of the view, in pixels.
     *
     * @return the view's width
     */
    int getWidth();

    /**
     * Returns the current height of the view, in pixels.
     *
     * @return the view's height
     */
    int getHeight();
}
