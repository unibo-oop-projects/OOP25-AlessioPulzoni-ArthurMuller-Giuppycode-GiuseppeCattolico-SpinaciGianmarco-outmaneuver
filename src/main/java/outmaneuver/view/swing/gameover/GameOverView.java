package outmaneuver.view.swing.gameover;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import outmaneuver.model.session.ScoreEntry;

public final class GameOverView extends JPanel {

    private static final int TITLE_FONT_SIZE   = 64;
    private static final int SCORE_FONT_SIZE   = 28;
    private static final int TABLE_FONT_SIZE   = 16;
    private static final int BUTTON_FONT_SIZE  = 20;
    private static final int BUTTON_WIDTH      = 200;
    private static final int BUTTON_HEIGHT     = 50;
    private static final int VGAP              = 14;
    private static final int TOP_ENTRIES       = 5;

    private final JLabel scoreLabel;
    private final JPanel tablePanel;

    public GameOverView(final Runnable onPlayAgain, final Runnable onMenu) {
        final Runnable safePlayAgain = Objects.requireNonNull(onPlayAgain);
        final Runnable safeMenu      = Objects.requireNonNull(onMenu);

        setBackground(Color.BLACK);
        setLayout(new GridBagLayout());

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx  = 0;
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(VGAP, 0, 0, 0);

        final JLabel title = new JLabel("GAME OVER", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, TITLE_FONT_SIZE));
        title.setForeground(Color.RED);

        scoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
        scoreLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, SCORE_FONT_SIZE));
        scoreLabel.setForeground(Color.WHITE);

        tablePanel = new JPanel(new GridBagLayout());
        tablePanel.setBackground(Color.BLACK);

        final JButton playAgainButton = new JButton("PLAY AGAIN");
        final JButton menuButton      = new JButton("MENU");
        final Dimension btnSize = new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);
        for (final JButton btn : new JButton[]{playAgainButton, menuButton}) {
            btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, BUTTON_FONT_SIZE));
            btn.setPreferredSize(btnSize);
        }
        playAgainButton.addActionListener(e -> safePlayAgain.run());
        menuButton.addActionListener(e -> safeMenu.run());

        gbc.gridy = 0; add(title,          gbc);
        gbc.gridy = 1; add(scoreLabel,      gbc);
        gbc.gridy = 2; add(tablePanel,      gbc);
        gbc.gridy = 3; add(playAgainButton, gbc);
        gbc.gridy = 4; add(menuButton,      gbc);
    }

    /**
     * Aggiorna il contenuto della schermata con i dati della partita appena conclusa.
     * Deve essere chiamato sull'EDT, subito prima di mostrare questa schermata.
     */
    public void show(final int finalScore, final List<ScoreEntry> topScores) {
        Objects.requireNonNull(topScores, "topScores must not be null");
        SwingUtilities.invokeLater(() -> {
            scoreLabel.setText("Score: " + finalScore);
            rebuildTable(topScores);
            revalidate();
            repaint();
        });
    }

    private void rebuildTable(final List<ScoreEntry> topScores) {
        tablePanel.removeAll();

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 12, 2, 12);
        gbc.gridy  = 0;

        addTableRow(gbc, "#", "Name", "Score", "Date", true);

        final int count = Math.min(topScores.size(), TOP_ENTRIES);
        for (int i = 0; i < count; i++) {
            final ScoreEntry e = topScores.get(i);
            addTableRow(gbc,
                    String.valueOf(i + 1),
                    e.playerName(),
                    String.valueOf(e.score()),
                    e.date().toString(),
                    false);
        }
    }

    private void addTableRow(final GridBagConstraints gbc,
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
            tablePanel.add(lbl, gbc);
        }
        gbc.gridy++;
    }
}

