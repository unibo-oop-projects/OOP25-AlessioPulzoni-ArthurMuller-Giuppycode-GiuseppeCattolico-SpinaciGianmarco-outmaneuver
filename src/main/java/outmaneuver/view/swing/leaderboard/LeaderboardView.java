package outmaneuver.view.swing.leaderboard;

import java.awt.Color;
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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import outmaneuver.model.session.ScoreEntry;

public final class LeaderboardView extends JPanel {

    private static final int TITLE_FONT_SIZE   = 48;
    private static final int BUTTON_FONT_SIZE  = 20;
    private static final int BUTTON_WIDTH      = 200;
    private static final int BUTTON_HEIGHT     = 50;
    private static final int VGAP              = 14;
    private static final int SCROLL_PANE_WIDTH = 600;
    private static final int SCROLL_PANE_HEIGHT = 220;

    private final Supplier<List<ScoreEntry>> scoresSupplier;
    private final LeaderboardTablePanel tablePanel;

    public LeaderboardView(final Supplier<List<ScoreEntry>> scoresSupplier, final Runnable onBack) {
        this.scoresSupplier = Objects.requireNonNull(scoresSupplier, "scoresSupplier must not be null");
        final Runnable safeBack = Objects.requireNonNull(onBack, "onBack must not be null");

        setBackground(Color.BLACK);
        setLayout(new GridBagLayout());

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx  = 0;
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(VGAP, 0, 0, 0);

        final JLabel title = new JLabel("LEADERBOARD", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, TITLE_FONT_SIZE));
        title.setForeground(Color.YELLOW);

        tablePanel = new LeaderboardTablePanel(Integer.MAX_VALUE);

        final JScrollPane scrollPane = new JScrollPane(tablePanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(SCROLL_PANE_WIDTH, SCROLL_PANE_HEIGHT));
        scrollPane.getViewport().setBackground(Color.BLACK);
        scrollPane.setBackground(Color.BLACK);
        scrollPane.setBorder(null);

        final JButton backButton = new JButton("BACK");
        backButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, BUTTON_FONT_SIZE));
        backButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        backButton.addActionListener(e -> safeBack.run());

        gbc.gridy = 0; add(title,      gbc);
        gbc.gridy = 1; add(scrollPane, gbc);
        gbc.gridy = 2; add(backButton, gbc);
    }

    /** Ricarica e mostra i punteggi aggiornati. Chiamare prima di mostrare questa schermata. */
    public void refresh() {
        SwingUtilities.invokeLater(() -> tablePanel.refresh(scoresSupplier.get()));
    }
}
