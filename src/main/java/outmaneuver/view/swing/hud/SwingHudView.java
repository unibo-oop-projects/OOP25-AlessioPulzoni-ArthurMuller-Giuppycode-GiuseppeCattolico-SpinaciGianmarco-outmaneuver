package outmaneuver.view.swing.hud;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import outmaneuver.assembler.ScreenAssembler.ScreenMetrics;
import outmaneuver.view.GameView;
import outmaneuver.view.HudSnapshot;

/**
 * Swing/AWT implementation of {@link IHudView}: draws the elapsed time, speed,
 * shield status, star count and a "PAUSED" overlay using {@link Graphics2D}.
 */
public final class SwingHudView implements IHudView {

    private static final int PAUSED_TEXT_RGB_COMPONENT = 255;

    private final ScreenMetrics metrics;

    /**
     * Creates the HUD renderer.
     *
     * @param metrics scaling metrics for the current window size
     */
    public SwingHudView(final ScreenMetrics metrics) {
        this.metrics = metrics;
    }

    @Override
    public void render(final Graphics2D g2d, final HudSnapshot hud,
                       final GameView view) {
        final int width = view.getWidth();
        final int height = view.getHeight();
        final int hudMargin = metrics.sw(12);
        final int hudLineHeight = metrics.sh(22);
        final Font hudFont = new Font(Font.MONOSPACED, Font.BOLD, metrics.sf(16));
        g2d.setFont(hudFont);
        final FontMetrics fm = g2d.getFontMetrics();

        final long totalSec = hud.elapsedMs() / 1000;
        final String timeStr = String.format("Time:   %02d:%02d", totalSec / 60, totalSec % 60);
        final String speedStr = String.format("Speed:  %.1f", hud.speed());
        final String shieldStr = "Shield: " + (hud.shieldActive() ? "ON" : "OFF");
        final String starsStr = "Stars: " + hud.stars();

        g2d.setColor(Color.WHITE);
        g2d.drawString(timeStr, hudMargin, hudMargin + hudLineHeight);
        g2d.drawString(speedStr, hudMargin, hudMargin + hudLineHeight * 2);

        g2d.setColor(hud.shieldActive() ? Color.CYAN : Color.GRAY);
        g2d.drawString(shieldStr, hudMargin, hudMargin + hudLineHeight * 3);

        g2d.setColor(Color.YELLOW);
        g2d.drawString(starsStr, width - fm.stringWidth(starsStr) - hudMargin, hudMargin + hudLineHeight);

        if (hud.paused()) {
            final Font pausedFont = new Font(Font.SANS_SERIF, Font.BOLD, metrics.sf(48));
            g2d.setFont(pausedFont);
            final FontMetrics pfm = g2d.getFontMetrics();
            final String pausedStr = "PAUSED";
            g2d.setColor(new Color(PAUSED_TEXT_RGB_COMPONENT, PAUSED_TEXT_RGB_COMPONENT, PAUSED_TEXT_RGB_COMPONENT, 180));
            g2d.drawString(pausedStr, (width - pfm.stringWidth(pausedStr)) / 2, height / 2);
        }
    }
}
