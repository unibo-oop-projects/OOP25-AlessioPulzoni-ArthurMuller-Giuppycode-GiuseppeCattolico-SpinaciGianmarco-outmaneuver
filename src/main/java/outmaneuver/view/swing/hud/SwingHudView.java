package outmaneuver.view.swing.hud;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import outmaneuver.view.GameView;
import outmaneuver.view.HudSnapshot;

public final class SwingHudView implements IHudView {

    private static final int HUD_MARGIN = 12;
    private static final int HUD_LINE_HEIGHT = 22;
    private static final int HUD_FONT_SIZE = 16;
    private static final int PAUSED_FONT_SIZE = 48;

    @Override
    public void render(final Graphics2D g2d, final HudSnapshot hud,
                       final GameView view) {
        final int width = view.getWidth();
        final int height = view.getHeight();
        final Font hudFont = new Font(Font.MONOSPACED, Font.BOLD, HUD_FONT_SIZE);
        g2d.setFont(hudFont);
        final FontMetrics fm = g2d.getFontMetrics();

        final long totalSec = hud.elapsedMs() / 1000;
        final String timeStr   = String.format("Time:   %02d:%02d", totalSec / 60, totalSec % 60);
        final String speedStr  = String.format("Speed:  %.1f", hud.speed());
        final String shieldStr = "Shield: " + (hud.shieldActive() ? "ON" : "OFF");
        final String starsStr  = "Stars: " + hud.stars();

        g2d.setColor(Color.WHITE);
        g2d.drawString(timeStr,  HUD_MARGIN, HUD_MARGIN + HUD_LINE_HEIGHT);
        g2d.drawString(speedStr, HUD_MARGIN, HUD_MARGIN + HUD_LINE_HEIGHT * 2);

        g2d.setColor(hud.shieldActive() ? Color.CYAN : Color.GRAY);
        g2d.drawString(shieldStr, HUD_MARGIN, HUD_MARGIN + HUD_LINE_HEIGHT * 3);

        g2d.setColor(Color.YELLOW);
        g2d.drawString(starsStr, width - fm.stringWidth(starsStr) - HUD_MARGIN, HUD_MARGIN + HUD_LINE_HEIGHT);

        if (hud.paused()) {
            final Font pausedFont = new Font(Font.SANS_SERIF, Font.BOLD, PAUSED_FONT_SIZE);
            g2d.setFont(pausedFont);
            final FontMetrics pfm = g2d.getFontMetrics();
            final String pausedStr = "PAUSED";
            g2d.setColor(new Color(255, 255, 255, 180));
            g2d.drawString(pausedStr, (width - pfm.stringWidth(pausedStr)) / 2, height / 2);
        }
    }
}
