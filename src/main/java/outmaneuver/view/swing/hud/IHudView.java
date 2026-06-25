package outmaneuver.view.swing.hud;

import java.awt.Graphics2D;

import outmaneuver.view.GameView;
import outmaneuver.view.HudSnapshot;

/**
 * Renders the in-game HUD (time, speed, shield, stars, pause overlay) on top of the
 * game scene.
 */
@FunctionalInterface
public interface IHudView {

    /**
     * Draws the HUD for the given snapshot onto the supplied graphics context.
     *
     * @param g2d graphics context to draw onto
     * @param hud HUD data to render
     * @param view game view providing the current viewport dimensions
     */
    void render(Graphics2D g2d, HudSnapshot hud, GameView view);
}
