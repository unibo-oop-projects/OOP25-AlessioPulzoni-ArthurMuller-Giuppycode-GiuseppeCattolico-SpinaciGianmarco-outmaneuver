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
import javax.swing.SwingConstants;

import outmaneuver.model.session.ScoreEntry;

public final class LeaderboardTablePanel extends JPanel {

    private static final int TABLE_FONT_SIZE = 16;

    private final int maxEntries;

    public LeaderboardTablePanel(final int maxEntries) {
        if (maxEntries <= 0) {
            throw new IllegalArgumentException("maxEntries must be positive, was: " + maxEntries);
        }
        this.maxEntries = maxEntries;
        setLayout(new GridBagLayout());
        setBackground(Color.BLACK);
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
                header ? Font.BOLD : Font.PLAIN, TABLE_FONT_SIZE);
        final Color color = header ? Color.YELLOW : Color.WHITE;

        int col = 0;
        for (final String text : new String[]{rank, name, score, date}) {
            final JLabel lbl = new JLabel(text, SwingConstants.CENTER);
            lbl.setFont(font);
            lbl.setForeground(color);
            gbc.gridx = col++;
            add(lbl, gbc);
        }
        gbc.gridy++;
    }
}
