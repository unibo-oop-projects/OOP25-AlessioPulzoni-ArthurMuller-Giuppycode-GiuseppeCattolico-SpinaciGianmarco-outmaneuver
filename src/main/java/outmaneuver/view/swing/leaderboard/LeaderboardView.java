package outmaneuver.view.swing.leaderboard;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Objects;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import outmaneuver.assembler.ScreenAssembler.ScreenMetrics;
import outmaneuver.model.session.ScoreEntry;
import outmaneuver.view.swing.Theme;

/**
 * Swing screen that shows the full leaderboard (up to the top 20 scores) in a
 * scrollable table, with a button to go back to the menu.
 */
@SuppressFBWarnings(
        value = "SE_BAD_FIELD",
        justification = "LeaderboardView is a Swing JPanel that is never actually serialized")
public final class LeaderboardView extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int TITLE_INSET = 14;
    private static final int TITLE_FONT_SIZE = 48;
    private static final int SCROLL_PANE_WIDTH = 800;
    private static final int SCROLL_PANE_HEIGHT = 420;

    private final Supplier<List<ScoreEntry>> scoresSupplier;
    private final LeaderboardTablePanel tablePanel;

    /**
     * Creates the leaderboard screen.
     *
     * @param metrics scaling metrics for the current window size
     * @param scoresSupplier supplies the current leaderboard entries when refreshed
     * @param onBack callback invoked when the player clicks "Back"
     */
    public LeaderboardView(final ScreenMetrics metrics, final Supplier<List<ScoreEntry>> scoresSupplier,
            final Runnable onBack) {
        this.scoresSupplier = Objects.requireNonNull(scoresSupplier, "scoresSupplier must not be null");
        final Runnable safeBack = Objects.requireNonNull(onBack, "onBack must not be null");

        setBackground(Theme.BACKGROUND);
        setLayout(new GridBagLayout());

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(metrics.sh(TITLE_INSET), 0, 0, 0);

        final JLabel title = Theme.outlinedLabel("LEADERBOARD",
                new Font(Font.SANS_SERIF, Font.BOLD, metrics.sf(TITLE_FONT_SIZE)), Theme.TEXT_ACCENT);
        final JLabel subtitle = Theme.outlinedLabel("TOP 20 SCORES",
                new Font(Font.SANS_SERIF, Font.BOLD, metrics.sf(Theme.FONT_BODY)), Theme.TEXT_ACCENT);

        tablePanel = new LeaderboardTablePanel(metrics, Integer.MAX_VALUE);

        final JScrollPane scrollPane = new JScrollPane(tablePanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(metrics.sw(SCROLL_PANE_WIDTH), metrics.sh(SCROLL_PANE_HEIGHT)));
        scrollPane.getViewport().setBackground(Theme.BACKGROUND);
        scrollPane.setBackground(Theme.BACKGROUND);
        scrollPane.setBorder(null);

        final JButton backButton = Theme.styledButton("BACK", metrics.sf(Theme.FONT_BUTTON),
                metrics.sw(Theme.BUTTON_WIDTH), metrics.sh(Theme.BUTTON_HEIGHT));
        backButton.addActionListener(e -> safeBack.run());

        gbc.gridy = 0;
        add(title, gbc);
        gbc.gridy = 1;
        add(subtitle, gbc);
        gbc.gridy = 2;
        add(scrollPane, gbc);
        gbc.gridy = 3;
        add(backButton, gbc);
    }

    /** Ricarica e mostra i punteggi aggiornati. Chiamare prima di mostrare questa schermata. */
    public void refresh() {
        SwingUtilities.invokeLater(() -> tablePanel.refresh(scoresSupplier.get()));
    }
}
