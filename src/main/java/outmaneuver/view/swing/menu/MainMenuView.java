package outmaneuver.view.swing.menu;

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

public class MainMenuView extends JPanel {

    private static final int TITLE_FONT_SIZE  = 64;
    private static final int BUTTON_FONT_SIZE = 20;
    private static final int BUTTON_WIDTH     = 200;
    private static final int BUTTON_HEIGHT    = 50;
    private static final int VGAP             = 20;

    public MainMenuView(final Runnable onStart, final Runnable onExit) {
        final Runnable safeStart = Objects.requireNonNull(onStart);
        final Runnable safeExit  = Objects.requireNonNull(onExit);

        setBackground(Color.BLACK);
        setLayout(new GridBagLayout());

        final JLabel title = new JLabel("OUTMANEUVER", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, TITLE_FONT_SIZE));
        title.setForeground(Color.WHITE);

        final JButton startButton = new JButton("START");
        final JButton exitButton  = new JButton("EXIT");
        startButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, BUTTON_FONT_SIZE));
        exitButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, BUTTON_FONT_SIZE));
        final Dimension btnSize = new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);
        startButton.setPreferredSize(btnSize);
        exitButton.setPreferredSize(btnSize);

        startButton.addActionListener(e -> safeStart.run());
        exitButton.addActionListener(e -> safeExit.run());

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill  = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(VGAP, 0, 0, 0);

        gbc.gridy = 0;
        add(title, gbc);
        gbc.gridy = 1;
        add(startButton, gbc);
        gbc.gridy = 2;
        add(exitButton, gbc);
    }
}
