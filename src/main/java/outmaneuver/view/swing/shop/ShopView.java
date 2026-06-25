package outmaneuver.view.swing.shop;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import outmaneuver.assembler.ScreenAssembler.ScreenMetrics;
import outmaneuver.model.area.entity.plane.PlaneStats;
import outmaneuver.model.shop.ShopItem;
import outmaneuver.util.assets.AssetStore;
import outmaneuver.util.assets.SpriteId;
import outmaneuver.view.swing.Theme;

/**
 * Swing screen for browsing the plane catalog, previewing stats, equipping owned
 * planes and purchasing new ones.
 */
@SuppressFBWarnings(
        value = "SE_BAD_FIELD",
        justification = "ShopView is a Swing JPanel that is never actually serialized")
public final class ShopView extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int TITLE_TOP_INSET = 12;
    private static final int TITLE_SIDE_INSET = 16;
    private static final int NAME_FONT_SIZE = 30;
    private static final int SPRITE_PREVIEW_SIZE = 96;
    private static final int NAV_BUTTON_SIZE = 48;
    private static final int ACTION_BUTTON_WIDTH = 160;
    private static final int BACK_ROW_GRIDY = 9;
    private static final int TURN_ROW_GRIDY = 5;
    private static final int PRICE_ROW_GRIDY = 6;
    private static final int FEEDBACK_ROW_GRIDY = 7;

    private final ScreenMetrics metrics;
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
    private final JButton buyBtn;

    private int currentIndex;

    /**
     * Creates the shop screen.
     *
     * @param metrics scaling metrics for the current window size
     * @param assets provides the sprites already loaded in memory
     * @param catalog non-empty list of items available in the shop
     * @param coinsSupplier supplies the player's current coin balance
     * @param equippedStatsSupplier supplies the stats of the currently equipped plane
     * @param isOwnedFn predicate that tells whether a plane id is already owned
     * @param onPurchase callback invoked to attempt a purchase/equip; returns whether it succeeded
     * @param onBack callback invoked when the player clicks "Back"
     */
    public ShopView(final ScreenMetrics metrics,
                    final AssetStore assets,
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
        this.metrics = metrics;
        this.assets = assets;
        this.catalog = List.copyOf(catalog);
        this.coinsSupplier = Objects.requireNonNull(coinsSupplier);
        this.equippedStatsSupplier = Objects.requireNonNull(equippedStatsSupplier);
        this.isOwnedFn = Objects.requireNonNull(isOwnedFn);
        this.onPurchase = Objects.requireNonNull(onPurchase);
        Objects.requireNonNull(onBack);

        setBackground(Theme.BACKGROUND);
        setLayout(new GridBagLayout());

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(metrics.sh(TITLE_TOP_INSET), metrics.sw(TITLE_SIDE_INSET), 0, metrics.sw(TITLE_SIDE_INSET));

        final JLabel title = Theme.outlinedLabel("SHOP", new Font(Font.SANS_SERIF, Font.BOLD, metrics.sf(48)), Theme.TEXT_TITLE);

        coinsLabel = Theme.outlinedLabel("Coins: 0",
                new Font(Font.MONOSPACED, Font.BOLD, metrics.sf(Theme.FONT_BUTTON)), Theme.TEXT_ACCENT);

        nameLabel = outlinedLabel("", metrics.sf(NAME_FONT_SIZE), Font.BOLD, Theme.TEXT_INFO);
        spriteLabel = new JLabel();
        spriteLabel.setHorizontalAlignment(JLabel.CENTER);
        spriteLabel.setPreferredSize(new Dimension(metrics.sw(SPRITE_PREVIEW_SIZE), metrics.sh(SPRITE_PREVIEW_SIZE)));
        speedLabel = outlinedLabel("", metrics.sf(Theme.FONT_BODY), Font.PLAIN, Theme.TEXT_TITLE);
        turnLabel = outlinedLabel("", metrics.sf(Theme.FONT_BODY), Font.PLAIN, Theme.TEXT_TITLE);
        priceLabel = outlinedLabel("", metrics.sf(Theme.FONT_BODY), Font.BOLD, Theme.TEXT_ACCENT);
        feedbackLabel = outlinedLabel("", metrics.sf(Theme.FONT_BODY), Font.BOLD, Theme.TEXT_SUCCESS);

        gbc.gridy = 0;
        add(title, gbc);
        gbc.gridy = 1;
        add(coinsLabel, gbc);
        gbc.gridy = 2;
        add(nameLabel, gbc);
        gbc.gridy = 3;
        add(spriteLabel, gbc);
        gbc.gridy = 4;
        add(speedLabel, gbc);
        gbc.gridy = TURN_ROW_GRIDY;
        add(turnLabel, gbc);
        gbc.gridy = PRICE_ROW_GRIDY;
        add(priceLabel, gbc);
        gbc.gridy = FEEDBACK_ROW_GRIDY;
        add(feedbackLabel, gbc);

        // navigation row: [←] [BUY] [→]
        gbc.gridy = 8;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        final int navSize = metrics.sh(NAV_BUTTON_SIZE);
        final JButton prevBtn = Theme.styledButton("<--", metrics.sf(Theme.FONT_BUTTON), navSize, navSize);
        final JButton nextBtn = Theme.styledButton("-->", metrics.sf(Theme.FONT_BUTTON), navSize, navSize);
        buyBtn = Theme.styledButton("BUY", metrics.sf(Theme.FONT_BUTTON),
                metrics.sw(ACTION_BUTTON_WIDTH), metrics.sh(NAV_BUTTON_SIZE));
        final JButton backBtn = Theme.styledButton("BACK", metrics.sf(Theme.FONT_BUTTON),
                metrics.sw(ACTION_BUTTON_WIDTH), metrics.sh(NAV_BUTTON_SIZE));

        gbc.gridx = 0;
        add(prevBtn, gbc);
        gbc.gridx = 1;
        add(buyBtn, gbc);
        gbc.gridx = 2;
        add(nextBtn, gbc);

        gbc.gridy = BACK_ROW_GRIDY;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
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
        nameLabel.setText(id.toUpperCase(Locale.ROOT));

        final SpriteId spriteId = SpriteId.fromFilename(item.stats().getSpriteId());
        final BufferedImage img = assets.getSprite(spriteId);
        final ImageIcon icon = new ImageIcon(img.getScaledInstance(
                metrics.sw(SPRITE_PREVIEW_SIZE), metrics.sh(SPRITE_PREVIEW_SIZE), java.awt.Image.SCALE_SMOOTH));
        spriteLabel.setIcon(icon);
        speedLabel.setText(String.format("Speed: %.0f   Radius: %.0f",
                item.stats().getBaseSpeed(), item.stats().getHitboxRadius()));
        turnLabel.setText(String.format("Turn rate: %.1f", item.stats().getTurnRate()));
        coinsLabel.setText("Coins: " + coinsSupplier.get());

        final boolean equipped = id.equals(equippedStatsSupplier.get().getId());
        final boolean owned = isOwnedFn.test(id);

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
