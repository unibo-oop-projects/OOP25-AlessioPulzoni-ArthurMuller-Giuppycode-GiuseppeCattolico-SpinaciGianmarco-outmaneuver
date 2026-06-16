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
import java.util.function.IntSupplier;
import java.util.function.Supplier;

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
    private final JLabel userLabel;
    private final JLabel planeLabel;

    public MainMenuView(final Supplier<String> playerNameSupplier,
                        final IntSupplier coinsSupplier,
                        final Supplier<String> equippedPlaneSupplier,
                        final Runnable onStart, final Runnable onShop,
                        final Runnable onLeaderboard, final Runnable onExit) {
        Objects.requireNonNull(playerNameSupplier);
        Objects.requireNonNull(coinsSupplier);
        Objects.requireNonNull(equippedPlaneSupplier);
        final Runnable safeStart       = Objects.requireNonNull(onStart);
        final Runnable safeShop        = Objects.requireNonNull(onShop);
        final Runnable safeLeaderboard = Objects.requireNonNull(onLeaderboard);
        final Runnable safeExit        = Objects.requireNonNull(onExit);

        setBackground(Color.BLACK);
        setLayout(new BorderLayout());

        // ── username top-right and wallet ──────────────────────────────────────────
        userLabel = new JLabel("", SwingConstants.LEFT);
        userLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, USERNAME_FONT_SIZE));
        userLabel.setForeground(Color.LIGHT_GRAY);

        coinsLabel = new JLabel("Coins: 0", SwingConstants.CENTER);
        coinsLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, COINS_FONT_SIZE));
        coinsLabel.setForeground(Color.YELLOW);

        final JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        topBar.setBackground(Color.BLACK);
        topBar.add(userLabel);
        topBar.add(coinsLabel);

        planeLabel = new JLabel("", SwingConstants.LEFT);
        planeLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, USERNAME_FONT_SIZE));
        planeLabel.setForeground(Color.CYAN);

        final JPanel topLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        topLeft.setBackground(Color.BLACK);
        topLeft.add(planeLabel);

        final JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.BLACK);
        topPanel.add(topLeft, BorderLayout.WEST);
        topPanel.add(topBar,  BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // ── main content ────────────────────────────────────────────────
        final JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(Color.BLACK);

        final JLabel title = new JLabel("OUTMANEUVER", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, TITLE_FONT_SIZE));
        title.setForeground(Color.WHITE);



        final JButton startButton       = new JButton("START");
        final JButton shopButton        = new JButton("SHOP");
        final JButton leaderboardButton = new JButton("LEADERBOARD");
        final JButton exitButton        = new JButton("EXIT");
        final Dimension btnSize = new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);
        for (final JButton btn : new JButton[]{startButton, shopButton, leaderboardButton, exitButton}) {
            btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, BUTTON_FONT_SIZE));
            btn.setPreferredSize(btnSize);
        }
        startButton.addActionListener(e -> safeStart.run());
        shopButton.addActionListener(e -> safeShop.run());
        leaderboardButton.addActionListener(e -> safeLeaderboard.run());
        exitButton.addActionListener(e -> safeExit.run());

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx  = 0;
        gbc.fill   = GridBagConstraints.NONE;
        gbc.insets = new Insets(VGAP, 0, 0, 0);

        gbc.gridy = 0; center.add(title,            gbc);
        gbc.gridy = 2; center.add(startButton,      gbc);
        gbc.gridy = 3; center.add(shopButton,        gbc);
        gbc.gridy = 4; center.add(leaderboardButton, gbc);
        gbc.gridy = 5; center.add(exitButton,        gbc);

        add(center, BorderLayout.CENTER);

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0
                    && isShowing()) {
                userLabel.setText("👤 " + playerNameSupplier.get());
                coinsLabel.setText("Coins: " + coinsSupplier.getAsInt());
                planeLabel.setText("Plane equipped: " + equippedPlaneSupplier.get());
            }
        });
    }
}
