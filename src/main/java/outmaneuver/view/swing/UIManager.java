package outmaneuver.view.swing;

import java.awt.CardLayout;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import javax.swing.JPanel;

import outmaneuver.model.session.GameState;

public final class UIManager extends JPanel {

    private final CardLayout cardLayout;
    private final Map<GameState, String> cardNames;

    public UIManager(final Map<GameState, JPanel> screens) {
        Objects.requireNonNull(screens, "screens must not be null");
        if (screens.isEmpty()) {
            throw new IllegalArgumentException("screens must not be empty");
        }

        this.cardLayout = new CardLayout();
        this.cardNames = new EnumMap<>(GameState.class);
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

    public void showScreen(final GameState state) {
        final String card = cardNames.get(Objects.requireNonNull(state));
        if (card == null) {
            throw new IllegalArgumentException("No screen registered for " + state);
        }
        cardLayout.show(this, card);
    }
}
