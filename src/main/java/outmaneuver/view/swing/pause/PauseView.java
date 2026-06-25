package outmaneuver.view.swing.pause;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import outmaneuver.factory.ScreenFactory.ScreenMetrics;
import outmaneuver.view.swing.Theme;

public final class PauseView extends JPanel {

    public PauseView(final ScreenMetrics metrics, final Runnable onResume, final Runnable onQuit) {
        Objects.requireNonNull(onResume, "onResume must not be null");
        Objects.requireNonNull(onQuit,   "onQuit must not be null");

        setBackground(new Color(0, 0, 0, 180));
        setLayout(new GridBagLayout());

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx  = 0;
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(metrics.sh(20), 0, 0, 0);

        final JLabel title = Theme.outlinedLabel("PAUSED", new Font(Font.SANS_SERIF, Font.BOLD, metrics.sf(64)), Theme.TEXT_ACCENT);

        final JButton resumeButton = Theme.styledButton("RESUME", metrics.sf(Theme.FONT_BUTTON), metrics.sw(Theme.BUTTON_WIDTH), metrics.sh(Theme.BUTTON_HEIGHT));
        final JButton quitButton   = Theme.styledButton("QUIT",   metrics.sf(Theme.FONT_BUTTON), metrics.sw(Theme.BUTTON_WIDTH), metrics.sh(Theme.BUTTON_HEIGHT));
        resumeButton.addActionListener(e -> onResume.run());
        quitButton.addActionListener(e -> onQuit.run());

        gbc.gridy = 0; add(title,        gbc);
        gbc.gridy = 1; add(resumeButton, gbc);
        gbc.gridy = 2; add(quitButton,   gbc);
    }
}
