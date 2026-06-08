package outmaneuver.view.swing;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.Objects;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import outmaneuver.model.missile.MissileRenderData;
import outmaneuver.view.EntityRenderData;
import outmaneuver.view.GameView;
import outmaneuver.view.HudSnapshot;
import outmaneuver.view.RenderState;

public final class SwingGameView implements GameView {

    private static final int PANEL_WIDTH          = 800;
    private static final int PANEL_HEIGHT         = 600;
    private static final int PLANE_RADIUS         = 20;
    private static final int DIR_INDICATOR_LENGTH = 40;
    private static final int HUD_MARGIN           = 12;
    private static final int HUD_LINE_HEIGHT      = 22;
    private static final int HUD_FONT_SIZE        = 16;
    private static final int PAUSED_FONT_SIZE     = 48;

    private final KeyListener keyListener;
    private final GamePanel gamePanel;
    private volatile RenderState latestState;

    public SwingGameView(final KeyListener keyListener) {
        this.keyListener = Objects.requireNonNull(keyListener, "keyListener must not be null");
        this.gamePanel   = new GamePanel();
        this.latestState = null;
    }

    public JPanel getPanel() { return gamePanel; }

    public void init() {
        gamePanel.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(keyListener);
    }

    @Override
    public void renderFrame(final RenderState state) {
        this.latestState = state;
        SwingUtilities.invokeLater(gamePanel::repaint);
    }

    private final class GamePanel extends JPanel {

        @Override
        protected void paintComponent(final Graphics g) {
            super.paintComponent(g);
            final Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setColor(new Color(20, 20, 40));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            final RenderState state = latestState;
            if (state != null) {
                final EntityRenderData planeData = state.getPlane();
                final double cameraX = planeData.getX();
                final double cameraY = planeData.getY();

                drawMissiles(g2d, state.getMissiles(), cameraX, cameraY);
                drawPlane(g2d, planeData, cameraX, cameraY);

                if (state.getHud() != null) {
                    drawHud(g2d, state.getHud());
                }
            }

            g2d.dispose();
        }

        private void drawHud(final Graphics2D g2d, final HudSnapshot hud) {
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
            g2d.drawString(starsStr, getWidth() - fm.stringWidth(starsStr) - HUD_MARGIN,
                    HUD_MARGIN + HUD_LINE_HEIGHT);

            if (hud.paused()) {
                final Font pausedFont = new Font(Font.SANS_SERIF, Font.BOLD, PAUSED_FONT_SIZE);
                g2d.setFont(pausedFont);
                final FontMetrics pfm = g2d.getFontMetrics();
                final String pausedStr = "PAUSED";
                g2d.setColor(new Color(255, 255, 255, 180));
                g2d.drawString(pausedStr,
                        (getWidth() - pfm.stringWidth(pausedStr)) / 2,
                        getHeight() / 2);
            }
        }

        private void drawPlane(final Graphics2D g2d, final EntityRenderData data,
                               final double cameraX, final double cameraY) {
            final int screenX = toScreenX(data.getX(), cameraX);
            final int screenY = toScreenY(data.getY(), cameraY);

            g2d.setColor(Color.CYAN);
            g2d.fillOval(screenX - PLANE_RADIUS, screenY - PLANE_RADIUS,
                    PLANE_RADIUS * 2, PLANE_RADIUS * 2);

            g2d.setColor(Color.WHITE);
            final int dirEndX = (int) Math.round(screenX + DIR_INDICATOR_LENGTH * Math.cos(data.getDirectionRad()));
            final int dirEndY = (int) Math.round(screenY + DIR_INDICATOR_LENGTH * Math.sin(data.getDirectionRad()));
            g2d.drawLine(screenX, screenY, dirEndX, dirEndY);
        }

        private void drawMissiles(final Graphics2D g2d,
                                  final List<MissileRenderData> missiles,
                                  final double cameraX, final double cameraY) {
            for (final MissileRenderData m : missiles) {
                final int sx = toScreenX(m.getWorldX(), cameraX);
                final int sy = toScreenY(m.getWorldY(), cameraY);
                final int r  = (int) m.getHitboxRadius();

                // Ghost invisibile — alpha bassissima
                final float alpha = (!m.isGhostVisible() && "ghost".equals(m.getMissileType()))
                        ? 0.12f : 1.0f;
                final Composite saved = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

                // Alone
                g2d.setColor(new Color(220, 60, 60, 60));
                g2d.fillOval(sx - r * 2, sy - r * 2, r * 4, r * 4);

                // Corpo
                g2d.setColor(getMissileColor(m.getMissileType()));
                g2d.fillOval(sx - r, sy - r, r * 2, r * 2);

                // Linea direzione
                if (m.getVx() != 0 || m.getVy() != 0) {
                    final double speed = Math.sqrt(m.getVx() * m.getVx() + m.getVy() * m.getVy());
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(1.5f));
                    g2d.drawLine(sx, sy,
                            (int) (sx + (m.getVx() / speed) * r * 1.8),
                            (int) (sy + (m.getVy() / speed) * r * 1.8));
                }

                // Barra lifetime
                if (m.getLifetimeRatio() >= 0) {
                    drawLifetimeBar(g2d, sx, sy, r, m.getLifetimeRatio());
                }

                g2d.setComposite(saved);
            }
        }

        private void drawLifetimeBar(final Graphics2D g2d, final int sx, final int sy,
                                     final int r, final double ratio) {
            final int barW = r * 2;
            final int barX = sx - r;
            final int barY = sy - r - 7;
            g2d.setColor(new Color(40, 40, 40, 180));
            g2d.fillRect(barX, barY, barW, 3);
            final Color c = ratio > 0.5 ? new Color(80, 220, 80)
                          : ratio > 0.2 ? new Color(220, 200, 0)
                          : new Color(220, 60, 60);
            g2d.setColor(c);
            g2d.fillRect(barX, barY, (int) (barW * ratio), 3);
        }

        private Color getMissileColor(final String type) {
            return switch (type) {
                case "fast"   -> new Color(255, 220, 0);
                case "sniper" -> new Color(220, 0, 220);
                case "bounce" -> new Color(30, 210, 90);
                case "ghost"  -> new Color(160, 80, 255);
                case "split"  -> new Color(255, 110, 0);
                case "freeze" -> new Color(130, 230, 255);
                case "shield" -> new Color(80, 160, 255);
                case "twins"  -> new Color(255, 220, 0);
                case "clock"  -> new Color(255, 215, 0);
                default       -> new Color(220, 60, 60);
            };
        }

        private int toScreenX(final double worldX, final double cameraX) {
            return (int) Math.round(worldX - cameraX + getWidth() / 2.0);
        }

        private int toScreenY(final double worldY, final double cameraY) {
            return (int) Math.round(worldY - cameraY + getHeight() / 2.0);
        }
    }
}