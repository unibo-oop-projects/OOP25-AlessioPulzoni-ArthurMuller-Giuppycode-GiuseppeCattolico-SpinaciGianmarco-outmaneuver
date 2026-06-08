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
import outmaneuver.view.swing.leaderboard.LeaderboardTablePanel;

public final class GameOverView extends JPanel {

    private static final int TITLE_FONT_SIZE   = 64;
    private static final int SCORE_FONT_SIZE   = 28;
    private static final int BUTTON_FONT_SIZE  = 20;
    private static final int BUTTON_WIDTH      = 200;
    private static final int BUTTON_HEIGHT     = 50;
    private static final int VGAP              = 14;

    private final JLabel scoreLabel;
    private final LeaderboardTablePanel tablePanel;

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

        tablePanel = new LeaderboardTablePanel(5);

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
            tablePanel.refresh(topScores);
            revalidate();
            repaint();
        });
    }
}

