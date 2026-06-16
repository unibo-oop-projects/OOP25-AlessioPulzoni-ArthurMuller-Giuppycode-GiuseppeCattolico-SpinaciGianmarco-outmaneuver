package outmaneuver.view.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyListener;
import java.util.Objects;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import outmaneuver.view.EntityRenderData;
import outmaneuver.view.GameView;
import outmaneuver.view.RenderState;
import outmaneuver.view.swing.hud.IHudView;

public final class SwingGameView extends JPanel implements GameView {

    private static final int PLANE_RADIUS = 20; // siamo sicuri qui??
    private static final int DIR_INDICATOR_LENGTH = 40; // siamo sicuri qui???

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
        final int screenX = (int) Math.round(data.getX() - cameraX + getWidth() / 2.0);
        final int screenY = (int) Math.round(data.getY() - cameraY + getHeight() / 2.0);
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(screenX - 10, screenY - 10, 20, 20);
    }

    private void drawPlane(final Graphics2D g2d, final EntityRenderData data,
            final double cameraX, final double cameraY) {
        final int screenX = (int) Math.round(data.getX() - cameraX + getWidth() / 2.0);
        final int screenY = (int) Math.round(data.getY() - cameraY + getHeight() / 2.0);

        g2d.setColor(Color.CYAN);
        g2d.fillOval(screenX - PLANE_RADIUS, screenY - PLANE_RADIUS,
                PLANE_RADIUS * 2, PLANE_RADIUS * 2);

        g2d.setColor(Color.WHITE);
        final int dirEndX = (int) Math.round(screenX + DIR_INDICATOR_LENGTH * Math.cos(data.getDirectionRad()));
        final int dirEndY = (int) Math.round(screenY + DIR_INDICATOR_LENGTH * Math.sin(data.getDirectionRad()));
        g2d.drawLine(screenX, screenY, dirEndX, dirEndY);
    }
}
