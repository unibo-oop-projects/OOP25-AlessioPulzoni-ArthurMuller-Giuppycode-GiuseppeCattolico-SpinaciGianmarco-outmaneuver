package outmaneuver.view.swing.menu;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import outmaneuver.assembler.ScreenAssembler.ScreenMetrics;
import outmaneuver.view.swing.Theme;

/**
 * Swing main menu screen: shows the player's name, coins and equipped plane, and
 * offers navigation to start a game, open the shop, view the leaderboard, or exit.
 */
public final class MainMenuView extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int LOGO_WIDTH = 600;
    private static final int BUTTONS_TOP_INSET = 20;
    private static final int EXIT_BUTTON_ROW_GRIDY = 5;

    private final JLabel coinsLabel;
    private final JLabel userLabel;
    private final JLabel planeLabel;

    /**
     * Creates the main menu screen.
     *
     * @param metrics scaling metrics for the current window size
     * @param playerNameSupplier supplies the player's display name
     * @param coinsSupplier supplies the player's current coin balance
     * @param equippedPlaneSupplier supplies the id of the currently equipped plane
     * @param onStart callback invoked when the player clicks "Start"
     * @param onShop callback invoked when the player clicks "Shop"
     * @param onLeaderboard callback invoked when the player clicks "Leaderboard"
     * @param onExit callback invoked when the player clicks "Exit"
     */
    public MainMenuView(final ScreenMetrics metrics,
                        final Supplier<String> playerNameSupplier,
                        final IntSupplier coinsSupplier,
                        final Supplier<String> equippedPlaneSupplier,
                        final Runnable onStart, final Runnable onShop,
                        final Runnable onLeaderboard, final Runnable onExit) {
        Objects.requireNonNull(playerNameSupplier);
        Objects.requireNonNull(coinsSupplier);
        Objects.requireNonNull(equippedPlaneSupplier);
        final Runnable safeStart = Objects.requireNonNull(onStart);
        final Runnable safeShop = Objects.requireNonNull(onShop);
        final Runnable safeLeaderboard = Objects.requireNonNull(onLeaderboard);
        final Runnable safeExit = Objects.requireNonNull(onExit);

        setBackground(Theme.BACKGROUND);
        setLayout(new BorderLayout());

        // ── username top-right and wallet ──────────────────────────────────────────
        userLabel = Theme.outlinedLabel("",
                new Font(Font.MONOSPACED, Font.PLAIN, metrics.sf(Theme.FONT_SMALL)), Theme.TEXT_BODY, SwingConstants.LEFT);

        coinsLabel = Theme.outlinedLabel("Coins: 0",
                new Font(Font.MONOSPACED, Font.BOLD, metrics.sf(Theme.FONT_BODY)), Theme.TEXT_ACCENT, SwingConstants.CENTER);

        final JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        topBar.setBackground(Theme.BACKGROUND);
        topBar.add(userLabel);
        topBar.add(coinsLabel);

        planeLabel = Theme.outlinedLabel("",
                new Font(Font.MONOSPACED, Font.PLAIN, metrics.sf(Theme.FONT_SMALL)), Theme.TEXT_INFO, SwingConstants.LEFT);

        final JPanel topLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        topLeft.setBackground(Theme.BACKGROUND);
        topLeft.add(planeLabel);

        final JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Theme.BACKGROUND);
        topPanel.add(topLeft, BorderLayout.WEST);
        topPanel.add(topBar, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // ── main content ────────────────────────────────────────────────
        final JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(Theme.BACKGROUND);

        final ImageIcon logoIcon = new ImageIcon(
            new ImageIcon(getClass().getResource("/assets/sprites/logo.png"))
                .getImage()
                .getScaledInstance(metrics.sw(LOGO_WIDTH), -1, Image.SCALE_SMOOTH)
        );
        final JLabel title = new JLabel(logoIcon);

        final JButton startButton = Theme.styledButton("START", metrics.sf(Theme.FONT_BUTTON),
                metrics.sw(Theme.BUTTON_WIDTH), metrics.sh(Theme.BUTTON_HEIGHT));
        final JButton shopButton = Theme.styledButton("SHOP", metrics.sf(Theme.FONT_BUTTON),
                metrics.sw(Theme.BUTTON_WIDTH), metrics.sh(Theme.BUTTON_HEIGHT));
        final JButton leaderboardButton = Theme.styledButton("LEADERBOARD", metrics.sf(Theme.FONT_BUTTON),
                metrics.sw(Theme.BUTTON_WIDTH), metrics.sh(Theme.BUTTON_HEIGHT));
        final JButton exitButton = Theme.styledButton("EXIT", metrics.sf(Theme.FONT_BUTTON),
                metrics.sw(Theme.BUTTON_WIDTH), metrics.sh(Theme.BUTTON_HEIGHT));
        startButton.addActionListener(e -> safeStart.run());
        shopButton.addActionListener(e -> safeShop.run());
        leaderboardButton.addActionListener(e -> safeLeaderboard.run());
        exitButton.addActionListener(e -> safeExit.run());

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(metrics.sh(BUTTONS_TOP_INSET), 0, 0, 0);

        gbc.gridy = 0;
        center.add(title, gbc);
        gbc.gridy = 2;
        center.add(startButton, gbc);
        gbc.gridy = 3;
        center.add(shopButton, gbc);
        gbc.gridy = 4;
        center.add(leaderboardButton, gbc);
        gbc.gridy = EXIT_BUTTON_ROW_GRIDY;
        center.add(exitButton, gbc);

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
