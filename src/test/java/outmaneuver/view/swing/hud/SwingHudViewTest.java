package outmaneuver.view.swing.hud;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.view.GameView;
import outmaneuver.view.HudSnapshot;
import outmaneuver.view.RenderState;

class SwingHudViewTest {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private IHudView hudView;
    private Graphics2D g2d;
    private GameView stubView;

    @BeforeEach
    void setUp() {
        hudView = new SwingHudView();
        g2d = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB).createGraphics();
        stubView = new GameView() {
            @Override public void renderFrame(final RenderState state) { }
            @Override public int getWidth() { return WIDTH; }
            @Override public int getHeight() { return HEIGHT; }
        };
    }

    @Test
    void renderDoesNotThrowWhenNotPaused() {
        final HudSnapshot hud = new HudSnapshot(65_000, 3.5, false, false, 2);
        assertDoesNotThrow(() -> hudView.render(g2d, hud, stubView));
    }

    @Test
    void renderDoesNotThrowWhenPaused() {
        final HudSnapshot hud = new HudSnapshot(30_000, 1.0, true, true, 0);
        assertDoesNotThrow(() -> hudView.render(g2d, hud, stubView));
    }

    @Test
    void renderDoesNotThrowWithZeroElapsed() {
        final HudSnapshot hud = new HudSnapshot(0, 0.0, false, false, 0);
        assertDoesNotThrow(() -> hudView.render(g2d, hud, stubView));
    }
}
