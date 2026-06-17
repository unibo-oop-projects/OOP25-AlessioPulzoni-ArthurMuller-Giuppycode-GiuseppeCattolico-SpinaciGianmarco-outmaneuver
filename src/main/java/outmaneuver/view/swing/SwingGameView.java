package outmaneuver.view.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.Objects;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import outmaneuver.util.Vector2;

import outmaneuver.view.EntityRenderData;
import outmaneuver.view.GameView;
import outmaneuver.view.MissileRenderData;
import outmaneuver.view.RenderState;
import outmaneuver.view.swing.hud.IHudView;

public final class SwingGameView extends JPanel implements GameView {

    private static final int PLANE_RADIUS = 20;
    private static final int DIR_INDICATOR_LENGTH = 40;

    private final KeyListener keyListener;
    private final IHudView hudView;
    private volatile RenderState latestState;

    public SwingGameView(final KeyListener keyListener, final IHudView hudView) {
        this.keyListener = Objects.requireNonNull(keyListener, "keyListener must not be null");
        this.hudView = Objects.requireNonNull(hudView, "hudView must not be null");
        this.latestState = null;
    }

    public void init() {
        setFocusable(true);
        addKeyListener(keyListener);
    }

    @Override
    public void renderFrame(final RenderState state) {
        this.latestState = state;
        SwingUtilities.invokeLater(this::repaint);
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        final var g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(20, 20, 40));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        final var state = latestState;
        if (state != null) {
            final var planeData = state.getPlane();
            final double cameraX = planeData.getX();
            final double cameraY = planeData.getY();

            drawMissiles(g2d, state.getMissiles(), cameraX, cameraY);

            for (final var col : state.getCollectibles()) {
                drawCollectible(g2d, col, cameraX, cameraY);
            }

            drawPlane(g2d, planeData, cameraX, cameraY);

            if (state.getHud() != null) {
                hudView.render(g2d, state.getHud(), getWidth(), getHeight());
            }
        }
        g2d.dispose();
    }

    private void drawCollectible(final Graphics2D g2d, final EntityRenderData data,
            final double cameraX, final double cameraY) {
        final int screenX = toScreenX(data.getX(), cameraX);
        final int screenY = toScreenY(data.getY(), cameraY);
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(screenX - 10, screenY - 10, 20, 20);
    }

    private void drawPlane(final Graphics2D g2d, final EntityRenderData data,
            final double cameraX, final double cameraY) {
        final int screenX = toScreenX(data.getX(), cameraX);
        final int screenY = toScreenY(data.getY(), cameraY);

        g2d.setColor(Color.CYAN);
        g2d.fillOval(screenX - PLANE_RADIUS, screenY - PLANE_RADIUS,
                PLANE_RADIUS * 2, PLANE_RADIUS * 2);

        g2d.setColor(Color.WHITE);
        final Vector2 dir = Vector2.fromAngle(data.getDirectionRad()).scale(DIR_INDICATOR_LENGTH);
        g2d.drawLine(screenX, screenY,
                (int) Math.round(screenX + dir.getX()),
                (int) Math.round(screenY + dir.getY()));
    }

    private void drawMissiles(final Graphics2D g2d,
                              final List<MissileRenderData> missiles,
                              final double cameraX, final double cameraY) {
        for (final MissileRenderData m : missiles) {
            final int sx = toScreenX(m.getWorldX(), cameraX);
            final int sy = toScreenY(m.getWorldY(), cameraY);
            final int r  = (int) m.getHitboxRadius();

            // Alone
            g2d.setColor(new Color(220, 60, 60, 60));
            g2d.fillOval(sx - r * 2, sy - r * 2, r * 4, r * 4);

            // Corpo
            g2d.setColor(getMissileColor(m.getMissileType()));
            g2d.fillOval(sx - r, sy - r, r * 2, r * 2);

            // Linea direzione
            final Vector2 vel = new Vector2(m.getVx(), m.getVy());
            if (vel.magnitude() > 0) {
                final Vector2 dir = vel.normalize();
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawLine(sx, sy,
                        (int) (sx + dir.getX() * r * 1.8),
                        (int) (sy + dir.getY() * r * 1.8));
            }

            // Barra lifetime
            if (m.getLifetimeRatio() >= 0) {
                drawLifetimeBar(g2d, sx, sy, r, m.getLifetimeRatio());
            }

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
            case "shield" -> new Color(80, 160, 255);
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
