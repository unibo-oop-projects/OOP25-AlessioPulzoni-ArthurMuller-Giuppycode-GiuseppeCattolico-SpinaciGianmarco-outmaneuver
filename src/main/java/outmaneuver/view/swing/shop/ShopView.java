package outmaneuver.view.swing.shop;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import outmaneuver.model.area.PlaneStats;
import outmaneuver.model.shop.ShopItem;

public final class ShopView extends JPanel {

    private static final int TITLE_FONT_SIZE   = 48;
    private static final int COINS_FONT_SIZE   = 20;
    private static final int INFO_FONT_SIZE    = 16;
    private static final int BUTTON_FONT_SIZE  = 20;
    private static final int NAV_FONT_SIZE     = 28;
    private static final int BUTTON_WIDTH      = 160;
    private static final int BUTTON_HEIGHT     = 48;
    private static final int NAV_SIZE          = 48;
    private static final int VGAP              = 12;
    private static final int HGAP              = 16;

    private final List<ShopItem> catalog;
    private final Supplier<Integer> coinsSupplier;
    private final Supplier<PlaneStats> equippedStatsSupplier;
    private final Predicate<String> isOwnedFn;
    private final Function<ShopItem, Boolean> onPurchase;

    private final JLabel coinsLabel;
    private final JLabel nameLabel;
    private final JLabel speedLabel;
    private final JLabel turnLabel;
    private final JLabel priceLabel;
    private final JLabel feedbackLabel;
    private JButton buyBtn;

    private int currentIndex;

    public ShopView(final List<ShopItem> catalog,
                    final Supplier<Integer> coinsSupplier,
                    final Supplier<PlaneStats> equippedStatsSupplier,
                    final Predicate<String> isOwnedFn,
                    final Function<ShopItem, Boolean> onPurchase,
                    final Runnable onBack) {
        Objects.requireNonNull(catalog, "catalog must not be null");
        if (catalog.isEmpty()) {
            throw new IllegalArgumentException("catalog must not be empty");
        }
        this.catalog               = List.copyOf(catalog);
        this.coinsSupplier         = Objects.requireNonNull(coinsSupplier);
        this.equippedStatsSupplier = Objects.requireNonNull(equippedStatsSupplier);
        this.isOwnedFn             = Objects.requireNonNull(isOwnedFn);
        this.onPurchase            = Objects.requireNonNull(onPurchase);
        Objects.requireNonNull(onBack);

        setBackground(Color.BLACK);
        setLayout(new GridBagLayout());

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx  = 0;
        gbc.gridwidth = 3;
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(VGAP, HGAP, 0, HGAP);

        final JLabel title = new JLabel("SHOP", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, TITLE_FONT_SIZE));
        title.setForeground(Color.WHITE);

        coinsLabel = new JLabel("Coins: 0", SwingConstants.CENTER);
        coinsLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, COINS_FONT_SIZE));
        coinsLabel.setForeground(Color.YELLOW);

        nameLabel  = label("", INFO_FONT_SIZE + 4, Font.BOLD,  Color.CYAN);
        speedLabel = label("", INFO_FONT_SIZE,      Font.PLAIN, Color.WHITE);
        turnLabel  = label("", INFO_FONT_SIZE,      Font.PLAIN, Color.WHITE);
        priceLabel = label("", INFO_FONT_SIZE,      Font.BOLD,  Color.YELLOW);
        feedbackLabel = label("", INFO_FONT_SIZE,   Font.BOLD,  Color.GREEN);

        gbc.gridy = 0; add(title,         gbc);
        gbc.gridy = 1; add(coinsLabel,    gbc);
        gbc.gridy = 2; add(nameLabel,     gbc);
        gbc.gridy = 3; add(speedLabel,    gbc);
        gbc.gridy = 4; add(turnLabel,     gbc);
        gbc.gridy = 5; add(priceLabel,    gbc);
        gbc.gridy = 6; add(feedbackLabel, gbc);

        // navigation row: [←] [BUY] [→]
        gbc.gridy    = 7;
        gbc.gridwidth = 1;
        gbc.fill     = GridBagConstraints.NONE;

        final JButton prevBtn = navButton("←");
        final JButton nextBtn = navButton("→");
        buyBtn = actionButton("BUY", BUTTON_WIDTH, BUTTON_HEIGHT);
        final JButton backBtn = actionButton("BACK", BUTTON_WIDTH, BUTTON_HEIGHT);

        gbc.gridx = 0; add(prevBtn, gbc);
        gbc.gridx = 1; add(buyBtn,  gbc);
        gbc.gridx = 2; add(nextBtn, gbc);

        gbc.gridy = 8; gbc.gridx = 0; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        add(backBtn, gbc);

        prevBtn.addActionListener(e -> navigate(-1));
        nextBtn.addActionListener(e -> navigate(+1));
        buyBtn.addActionListener(e -> buy());
        backBtn.addActionListener(e -> onBack.run());

        refreshDisplay();
    }

    /** Aggiorna il saldo mostrato. Chiamare prima di mostrare questa schermata. */
    public void refreshCoins() {
        SwingUtilities.invokeLater(() -> coinsLabel.setText("Coins: " + coinsSupplier.get()));
    }

    private void navigate(final int delta) {
        currentIndex = Math.floorMod(currentIndex + delta, catalog.size());
        feedbackLabel.setText("");
        refreshDisplay();
    }

    private void buy() {
        final boolean success = onPurchase.apply(catalog.get(currentIndex));
        if (success) {
            feedbackLabel.setForeground(Color.GREEN);
            feedbackLabel.setText("Purchased!");
        } else {
            feedbackLabel.setForeground(Color.RED);
            feedbackLabel.setText("Insufficient coins!");
        }
        refreshDisplay();
    }

    private void refreshDisplay() {
        final ShopItem item = catalog.get(currentIndex);
        final String id = item.stats().getId();
        nameLabel.setText(id.toUpperCase());
        speedLabel.setText(String.format("Speed: %.0f   Radius: %.0f",
                item.stats().getBaseSpeed(), item.stats().getHitboxRadius()));
        turnLabel.setText(String.format("Turn rate: %.1f", item.stats().getTurnRate()));
        coinsLabel.setText("Coins: " + coinsSupplier.get());

        final boolean equipped = id.equals(equippedStatsSupplier.get().getId());
        final boolean owned    = isOwnedFn.test(id);

        if (equipped) {
            priceLabel.setText("FREE");
            buyBtn.setText("EQUIPPED");
            buyBtn.setEnabled(false);
        } else if (owned) {
            priceLabel.setText("FREE");
            buyBtn.setText("EQUIP");
            buyBtn.setEnabled(true);
        } else {
            priceLabel.setText("Price: " + item.price() + " coins");
            buyBtn.setText("BUY");
            buyBtn.setEnabled(true);
        }
    }

    private static JLabel label(final String text, final int size, final int style, final Color color) {
        final JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font(Font.MONOSPACED, style, size));
        lbl.setForeground(color);
        return lbl;
    }

    private static JButton navButton(final String text) {
        final JButton btn = new JButton(text);
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, NAV_FONT_SIZE));
        btn.setPreferredSize(new Dimension(NAV_SIZE, NAV_SIZE));
        return btn;
    }

    private static JButton actionButton(final String text, final int w, final int h) {
        final JButton btn = new JButton(text);
        btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, BUTTON_FONT_SIZE));
        btn.setPreferredSize(new Dimension(w, h));
        return btn;
    }
}
