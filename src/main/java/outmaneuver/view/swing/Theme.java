package outmaneuver.view.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * Centralizes the colors, fonts and dimensions shared by the game's Swing screens,
 * and provides factory methods for the themed widgets used throughout the UI.
 */
public final class Theme {

    public static final Color BACKGROUND = new Color(180, 225, 245);

    public static final Color TEXT_TITLE = Color.WHITE;
    public static final Color TEXT_BODY = Color.LIGHT_GRAY;
    public static final Color TEXT_ACCENT = Color.YELLOW;
    public static final Color TEXT_INFO = Color.CYAN;
    public static final Color TEXT_ERROR = Color.RED;
    public static final Color TEXT_SUCCESS = Color.GREEN;

    public static final int FONT_BUTTON = 26;
    public static final int FONT_BODY = 26;
    public static final int FONT_SMALL = 20;

    public static final int BUTTON_WIDTH = 200;
    public static final int BUTTON_HEIGHT = 50;

    private static final Color BTN_BG = new Color(200, 70, 30);
    private static final Color BTN_BG_HOVER = new Color(230, 100, 50);
    private static final Color BTN_FG = Color.WHITE;
    private static final Color BTN_BORDER = new Color(160, 50, 20);

    private Theme() { }

    /**
     * Creates a themed button with the given text, font size and fixed dimensions,
     * including hover-color feedback.
     *
     * @param text label shown on the button
     * @param fontSize font size to use for the label
     * @param width preferred width of the button, in pixels
     * @param height preferred height of the button, in pixels
     * @return the configured button
     */
    public static JButton styledButton(final String text, final int fontSize, final int width, final int height) {
        final JButton btn = new JButton(text);
        btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
        btn.setPreferredSize(new Dimension(width, height));
        btn.setBackground(BTN_BG);
        btn.setForeground(BTN_FG);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BTN_BORDER, 1),
            BorderFactory.createEmptyBorder(4, 16, 4, 16)
        ));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(final java.awt.event.MouseEvent e) {
                btn.setBackground(BTN_BG_HOVER);
            }

            @Override
            public void mouseExited(final java.awt.event.MouseEvent e) {
                btn.setBackground(BTN_BG);
            }
        });
        return btn;
    }

    /**
     * Creates a label whose text is drawn with a black outline for readability against
     * any background, aligned as requested.
     *
     * @param text text to display
     * @param font font to render the text with
     * @param foreground fill color of the text
     * @param horizontalAlignment one of the {@link SwingConstants} horizontal alignment values
     * @return the configured label
     */
    public static JLabel outlinedLabel(final String text, final Font font, final Color foreground,
            final int horizontalAlignment) {
        final JLabel label = new JLabel(text, horizontalAlignment) {
            @Override
            protected void paintComponent(final Graphics g) {
                final String txt = getText();
                if (txt == null || txt.isEmpty()) {
                    super.paintComponent(g);
                    return;
                }
                final Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                     RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setFont(getFont());
                final FontMetrics fm = g2d.getFontMetrics();
                final float y = (getHeight() - fm.getHeight()) / 2f + fm.getAscent();
                final float x;
                final int sw = fm.stringWidth(txt);
                switch (horizontalAlignment) {
                    case LEFT:
                        x = 0f;
                        break;
                    case RIGHT:
                        x = getWidth() - sw;
                        break;
                    default:
                        x = (getWidth() - sw) / 2f;
                }
                g2d.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.setColor(Color.BLACK);
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx != 0 || dy != 0) {
                            g2d.drawString(txt, x + dx, y + dy);
                        }
                    }
                }
                g2d.setColor(foreground);
                g2d.drawString(txt, x, y);
                g2d.dispose();
            }
        };
        label.setFont(font);
        return label;
    }

    /**
     * Creates a center-aligned outlined label. Shorthand for
     * {@link #outlinedLabel(String, Font, Color, int)} with {@link SwingConstants#CENTER}.
     *
     * @param text text to display
     * @param font font to render the text with
     * @param foreground fill color of the text
     * @return the configured label
     */
    public static JLabel outlinedLabel(final String text, final Font font, final Color foreground) {
        return outlinedLabel(text, font, foreground, SwingConstants.CENTER);
    }
}
