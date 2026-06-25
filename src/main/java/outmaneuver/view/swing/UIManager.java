package outmaneuver.view.swing;

import java.awt.CardLayout;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import javax.swing.JPanel;

/**
 * Swaps between the game's Swing screens using a {@link CardLayout}, keyed by
 * {@link ScreenId}.
 */
public final class UIManager extends JPanel {

    private static final long serialVersionUID = 1L;

    private final CardLayout cardLayout;
    private final Map<ScreenId, String> cardNames;

    /**
     * Creates the manager and registers every screen with its corresponding card.
     *
     * @param screens non-empty map from screen identifier to its Swing panel
     */
    public UIManager(final Map<ScreenId, JPanel> screens) {
        Objects.requireNonNull(screens, "screens must not be null");
        if (screens.isEmpty()) {
            throw new IllegalArgumentException("screens must not be empty");
        }

        this.cardLayout = new CardLayout();
        this.cardNames = new EnumMap<>(ScreenId.class);
        setLayout(cardLayout);

        final Map<JPanel, String> registered = new java.util.IdentityHashMap<>();
        screens.forEach((state, panel) -> {
            Objects.requireNonNull(state, "screen state must not be null");
            Objects.requireNonNull(panel, "screen panel must not be null");
            final String cardName = registered.computeIfAbsent(panel, p -> {
                final String name = state.name();
                add(p, name);
                return name;
            });
            cardNames.put(state, cardName);
        });
    }

    /**
     * Switches the visible card to the screen registered for the given identifier.
     *
     * @param state identifier of the screen to show
     */
    public void showScreen(final ScreenId state) {
        final String card = cardNames.get(Objects.requireNonNull(state));
        if (card == null) {
            throw new IllegalArgumentException("No screen registered for " + state);
        }
        cardLayout.show(this, card);
    }
}
