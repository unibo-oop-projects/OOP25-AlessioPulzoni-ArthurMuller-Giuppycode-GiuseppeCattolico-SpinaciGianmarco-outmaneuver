package outmaneuver.view.swing.hud;

import java.awt.Graphics2D;

import outmaneuver.view.GameView;
import outmaneuver.view.HudSnapshot;

public interface IHudView {

    void render(Graphics2D g2d, HudSnapshot hud, GameView view);
}
