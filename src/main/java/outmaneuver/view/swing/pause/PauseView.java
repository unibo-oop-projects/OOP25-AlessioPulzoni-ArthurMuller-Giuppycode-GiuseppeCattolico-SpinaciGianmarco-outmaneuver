package outmaneuver.view.swing.pause;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public final class PauseView extends JPanel {

    private static final int TITLE_FONT_SIZE  = 64;
    private static final int BUTTON_FONT_SIZE = 20;
    private static final int BUTTON_WIDTH     = 200;
    private static final int BUTTON_HEIGHT    = 50;
    private static final int VGAP             = 20;

    public PauseView(final Runnable onResume, final Runnable onQuit) {
        Objects.requireNonNull(onResume, "onResume must not be null");
        Objects.requireNonNull(onQuit,   "onQuit must not be null");

        setBackground(new Color(0, 0, 0, 180));
        setLayout(new GridBagLayout());

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx  = 0;
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(VGAP, 0, 0, 0);

        final JLabel title = new JLabel("PAUSED", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, TITLE_FONT_SIZE));
        title.setForeground(Color.YELLOW);

        final JButton resumeButton = new JButton("RESUME");
        final JButton quitButton   = new JButton("QUIT");
        final Dimension btnSize = new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);
        for (final JButton btn : new JButton[]{resumeButton, quitButton}) {
            btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, BUTTON_FONT_SIZE));
            btn.setPreferredSize(btnSize);
        }
        resumeButton.addActionListener(e -> onResume.run());
        quitButton.addActionListener(e -> onQuit.run());

        gbc.gridy = 0; add(title,        gbc);
        gbc.gridy = 1; add(resumeButton, gbc);
        gbc.gridy = 2; add(quitButton,   gbc);
    }
}
