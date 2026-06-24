package outmaneuver.view.swing.shop;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import outmaneuver.model.area.entity.plane.PlaneStats;
import outmaneuver.model.shop.ShopItem;
import outmaneuver.util.assets.AssetStore;
import outmaneuver.util.assets.SpriteId;
import outmaneuver.view.swing.Theme;

public final class ShopView extends JPanel {

    private static final int TITLE_FONT_SIZE   = 48;
    private static final int COINS_FONT_SIZE   = 20;
    private static final int INFO_FONT_SIZE    = 16;
    private static final int BUTTON_WIDTH      = 160;
    private static final int BUTTON_HEIGHT     = 48;
    private static final int NAV_SIZE          = 48;
    private static final int VGAP              = 12;
    private static final int HGAP              = 16;

    private final AssetStore assets;
    private final List<ShopItem> catalog;
    private final Supplier<Integer> coinsSupplier;
    private final Supplier<PlaneStats> equippedStatsSupplier;
    private final Predicate<String> isOwnedFn;
    private final Function<ShopItem, Boolean> onPurchase;

    private final JLabel coinsLabel;
    private final JLabel nameLabel;
    private final JLabel spriteLabel;
    private final JLabel speedLabel;
    private final JLabel turnLabel;
    private final JLabel priceLabel;
    private final JLabel feedbackLabel;
    private JButton buyBtn;

    private int currentIndex;

    public ShopView(final AssetStore assets,
                    final List<ShopItem> catalog,
                    final Supplier<Integer> coinsSupplier,
                    final Supplier<PlaneStats> equippedStatsSupplier,
                    final Predicate<String> isOwnedFn,
                    final Function<ShopItem, Boolean> onPurchase,
                    final Runnable onBack) {
        Objects.requireNonNull(assets, "assets must not be null");
        Objects.requireNonNull(catalog, "catalog must not be null");
        if (catalog.isEmpty()) {
            throw new IllegalArgumentException("catalog must not be empty");
        }
        this.assets                = assets;
        this.catalog               = List.copyOf(catalog);
        this.coinsSupplier         = Objects.requireNonNull(coinsSupplier);
        this.equippedStatsSupplier = Objects.requireNonNull(equippedStatsSupplier);
        this.isOwnedFn             = Objects.requireNonNull(isOwnedFn);
        this.onPurchase            = Objects.requireNonNull(onPurchase);
        Objects.requireNonNull(onBack);

        setBackground(Theme.BACKGROUND);
        setLayout(new GridBagLayout());

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx  = 0;
        gbc.gridwidth = 3;
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(VGAP, HGAP, 0, HGAP);

        final JLabel title = Theme.outlinedLabel("SHOP", new Font(Font.SANS_SERIF, Font.BOLD, TITLE_FONT_SIZE), Theme.TEXT_TITLE);

        coinsLabel = Theme.outlinedLabel("Coins: 0", new Font(Font.MONOSPACED, Font.BOLD, Theme.FONT_BUTTON), Theme.TEXT_ACCENT);

        nameLabel  = outlinedLabel("", Theme.FONT_BODY + 4, Font.BOLD,  Theme.TEXT_INFO);
        spriteLabel = new JLabel();
        spriteLabel.setHorizontalAlignment(JLabel.CENTER);
        spriteLabel.setPreferredSize(new Dimension(96, 96));
        speedLabel = outlinedLabel("", Theme.FONT_BODY,      Font.PLAIN, Theme.TEXT_TITLE);
        turnLabel  = outlinedLabel("", Theme.FONT_BODY,      Font.PLAIN, Theme.TEXT_TITLE);
        priceLabel = outlinedLabel("", Theme.FONT_BODY,      Font.BOLD,  Theme.TEXT_ACCENT);
        feedbackLabel = outlinedLabel("", Theme.FONT_BODY,   Font.BOLD,  Theme.TEXT_SUCCESS);

        gbc.gridy = 0; add(title,         gbc);
        gbc.gridy = 1; add(coinsLabel,    gbc);
        gbc.gridy = 2; add(nameLabel,     gbc);
        gbc.gridy = 3; add(spriteLabel,   gbc);
        gbc.gridy = 4; add(speedLabel,    gbc);
        gbc.gridy = 5; add(turnLabel,     gbc);
        gbc.gridy = 6; add(priceLabel,    gbc);
        gbc.gridy = 7; add(feedbackLabel, gbc);

        // navigation row: [←] [BUY] [→]
        gbc.gridy    = 8;
        gbc.gridwidth = 1;
        gbc.fill     = GridBagConstraints.NONE;

        final JButton prevBtn = Theme.styledButton("<--", Theme.FONT_BUTTON, NAV_SIZE, NAV_SIZE);
        final JButton nextBtn = Theme.styledButton("-->", Theme.FONT_BUTTON, NAV_SIZE, NAV_SIZE);
        buyBtn = Theme.styledButton("BUY", Theme.FONT_BUTTON, BUTTON_WIDTH, BUTTON_HEIGHT);
        final JButton backBtn = Theme.styledButton("BACK", Theme.FONT_BUTTON, BUTTON_WIDTH, BUTTON_HEIGHT);

        gbc.gridx = 0; add(prevBtn, gbc);
        gbc.gridx = 1; add(buyBtn,  gbc);
        gbc.gridx = 2; add(nextBtn, gbc);

        gbc.gridy = 9; gbc.gridx = 0; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
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
        final ShopItem item = catalog.get(currentIndex);
        final boolean alreadyOwned = isOwnedFn.test(item.stats().getId());
        final boolean success = onPurchase.apply(item);
        if (success) {
            feedbackLabel.setForeground(Theme.TEXT_SUCCESS);
            feedbackLabel.setText(alreadyOwned ? "Equipped!" : "Purchased!");
        } else {
            feedbackLabel.setForeground(Theme.TEXT_ERROR);
            feedbackLabel.setText("Insufficient coins!");
        }
        refreshDisplay();
    }

    private void refreshDisplay() {
        final ShopItem item = catalog.get(currentIndex);
        final String id = item.stats().getId();
        nameLabel.setText(id.toUpperCase());

        final SpriteId spriteId = SpriteId.fromFilename(item.stats().getSpriteId());
        final BufferedImage img = assets.getSprite(spriteId);
        final ImageIcon icon = new ImageIcon(img.getScaledInstance(96, 96, java.awt.Image.SCALE_SMOOTH));
        spriteLabel.setIcon(icon);
        speedLabel.setText(String.format("Speed: %.0f   Radius: %.0f",
                item.stats().getBaseSpeed(), item.stats().getHitboxRadius()));
        turnLabel.setText(String.format("Turn rate: %.1f", item.stats().getTurnRate()));
        coinsLabel.setText("Coins: " + coinsSupplier.get());

        final boolean equipped = id.equals(equippedStatsSupplier.get().getId());
        final boolean owned    = isOwnedFn.test(id);

        if (equipped) {
            priceLabel.setText("OWNED");
            buyBtn.setText("EQUIPPED");
            buyBtn.setEnabled(false);
        } else if (owned) {
            priceLabel.setText("OWNED");
            buyBtn.setText("EQUIP");
            buyBtn.setEnabled(true);
        } else {
            priceLabel.setText("Price: " + item.price() + " coins");
            buyBtn.setText("BUY");
            buyBtn.setEnabled(true);
        }
    }

    private static JLabel outlinedLabel(final String text, final int size, final int style, final Color color) {
        return Theme.outlinedLabel(text, new Font(Font.MONOSPACED, style, size), color);
    }


}
