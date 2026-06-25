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

import outmaneuver.model.session.ScoreEntry;
import outmaneuver.factory.ScreenFactory.ScreenMetrics;
import outmaneuver.view.swing.Theme;

public final class LeaderboardView extends JPanel {

    private final Supplier<List<ScoreEntry>> scoresSupplier;
    private final LeaderboardTablePanel tablePanel;

    public LeaderboardView(final ScreenMetrics metrics, final Supplier<List<ScoreEntry>> scoresSupplier, final Runnable onBack) {
        this.scoresSupplier = Objects.requireNonNull(scoresSupplier, "scoresSupplier must not be null");
        final Runnable safeBack = Objects.requireNonNull(onBack, "onBack must not be null");

        setBackground(Theme.BACKGROUND);
        setLayout(new GridBagLayout());

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx  = 0;
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(metrics.sh(14), 0, 0, 0);

        final JLabel title = Theme.outlinedLabel("LEADERBOARD", new Font(Font.SANS_SERIF, Font.BOLD, metrics.sf(48)), Theme.TEXT_ACCENT);
        final JLabel subtitle = Theme.outlinedLabel("TOP 20 SCORES", new Font(Font.SANS_SERIF, Font.BOLD, metrics.sf(Theme.FONT_BODY)), Theme.TEXT_ACCENT);

        tablePanel = new LeaderboardTablePanel(metrics, Integer.MAX_VALUE);

        final JScrollPane scrollPane = new JScrollPane(tablePanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(metrics.sw(800), metrics.sh(420)));
        scrollPane.getViewport().setBackground(Theme.BACKGROUND);
        scrollPane.setBackground(Theme.BACKGROUND);
        scrollPane.setBorder(null);

        final JButton backButton = Theme.styledButton("BACK", metrics.sf(Theme.FONT_BUTTON), metrics.sw(Theme.BUTTON_WIDTH), metrics.sh(Theme.BUTTON_HEIGHT));
        backButton.addActionListener(e -> safeBack.run());

        gbc.gridy = 0; add(title,      gbc);
        gbc.gridy = 1; add(subtitle,   gbc);
        gbc.gridy = 2; add(scrollPane, gbc);
        gbc.gridy = 3; add(backButton, gbc);
    }

    /** Ricarica e mostra i punteggi aggiornati. Chiamare prima di mostrare questa schermata. */
    public void refresh() {
        SwingUtilities.invokeLater(() -> tablePanel.refresh(scoresSupplier.get()));
    }
}
