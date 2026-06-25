package outmaneuver.view.swing.leaderboard;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Objects;

import javax.swing.JLabel;
import javax.swing.JPanel;

import outmaneuver.model.session.ScoreEntry;
import outmaneuver.factory.ScreenFactory.ScreenMetrics;
import outmaneuver.view.swing.Theme;

public final class LeaderboardTablePanel extends JPanel {

    private final int maxEntries;
    private final ScreenMetrics metrics;

    public LeaderboardTablePanel(final ScreenMetrics metrics, final int maxEntries) {
        if (maxEntries <= 0) {
            throw new IllegalArgumentException("maxEntries must be positive, was: " + maxEntries);
        }
        this.maxEntries = maxEntries;
        this.metrics = metrics;
        setLayout(new GridBagLayout());
        setBackground(Theme.BACKGROUND);
    }

    /** Ricostruisce la tabella con i punteggi forniti. */
    public void refresh(final List<ScoreEntry> scores) {
        Objects.requireNonNull(scores, "scores must not be null");
        removeAll();

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 12, 2, 12);
        gbc.gridy  = 0;

        addRow(gbc, "#", "Name", "Score", "Date", true);

        final int count = Math.min(scores.size(), maxEntries);
        for (int i = 0; i < count; i++) {
            final ScoreEntry e = scores.get(i);
            addRow(gbc,
                    String.valueOf(i + 1),
                    e.playerName(),
                    String.valueOf(e.score()),
                    e.date().toString(),
                    false);
        }

        revalidate();
        repaint();
    }

    private void addRow(final GridBagConstraints gbc,
                        final String rank, final String name,
                        final String score, final String date,
                        final boolean header) {
        final Font font = new Font(Font.MONOSPACED,
                header ? Font.BOLD : Font.PLAIN, metrics.sf(Theme.FONT_BODY));
        final Color color = header ? Theme.TEXT_ACCENT : Theme.TEXT_TITLE;

        int col = 0;
        for (final String text : new String[]{rank, name, score, date}) {
            final JLabel lbl = Theme.outlinedLabel(text, font, color);
            gbc.gridx = col++;
            add(lbl, gbc);
        }
        gbc.gridy++;
    }
}
