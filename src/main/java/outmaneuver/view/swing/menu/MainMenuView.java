package outmaneuver.view.swing.menu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public final class MainMenuView extends JPanel {

    private static final int TITLE_FONT_SIZE    = 64;
    private static final int COINS_FONT_SIZE    = 16;
    private static final int USERNAME_FONT_SIZE = 14;
    private static final int BUTTON_FONT_SIZE   = 20;
    private static final int BUTTON_WIDTH       = 200;
    private static final int BUTTON_HEIGHT      = 50;
    private static final int VGAP               = 20;

    private final JLabel coinsLabel;

    public MainMenuView(final String playerName,
                        final Runnable onStart, final Runnable onShop, final Runnable onExit) {
        Objects.requireNonNull(playerName);
        final Runnable safeStart = Objects.requireNonNull(onStart);
        final Runnable safeShop  = Objects.requireNonNull(onShop);
        final Runnable safeExit  = Objects.requireNonNull(onExit);

        setBackground(Color.BLACK);
        setLayout(new BorderLayout());

        // ── username top-right and wallet ──────────────────────────────────────────
        final JLabel userLabel = new JLabel("\uD83D\uDC64 " + playerName);
        userLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, USERNAME_FONT_SIZE));
        userLabel.setForeground(Color.LIGHT_GRAY);

        coinsLabel = new JLabel("Coins: 0", SwingConstants.CENTER);
        coinsLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, COINS_FONT_SIZE));
        coinsLabel.setForeground(Color.YELLOW);

        final JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        topBar.setBackground(Color.BLACK);
        topBar.add(userLabel);
        topBar.add(coinsLabel);
        add(topBar, BorderLayout.NORTH);

        // ── main content ────────────────────────────────────────────────
        final JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(Color.BLACK);

        final JLabel title = new JLabel("OUTMANEUVER", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, TITLE_FONT_SIZE));
        title.setForeground(Color.WHITE);



        final JButton startButton = new JButton("START");
        final JButton shopButton  = new JButton("SHOP");
        final JButton exitButton  = new JButton("EXIT");
        final Dimension btnSize = new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);
        for (final JButton btn : new JButton[]{startButton, shopButton, exitButton}) {
            btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, BUTTON_FONT_SIZE));
            btn.setPreferredSize(btnSize);
        }
        startButton.addActionListener(e -> safeStart.run());
        shopButton.addActionListener(e -> safeShop.run());
        exitButton.addActionListener(e -> safeExit.run());

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx  = 0;
        gbc.fill   = GridBagConstraints.NONE;
        gbc.insets = new Insets(VGAP, 0, 0, 0);

        gbc.gridy = 0; center.add(title,       gbc);
        gbc.gridy = 2; center.add(startButton, gbc);
        gbc.gridy = 3; center.add(shopButton,  gbc);
        gbc.gridy = 4; center.add(exitButton,  gbc);

        add(center, BorderLayout.CENTER);
    }

    /** Aggiorna il saldo monete mostrato. Chiamare prima di mostrare questa schermata. */
    public void refreshCoins(final int coins) {
        coinsLabel.setText("Coins: " + coins);
    }
}
