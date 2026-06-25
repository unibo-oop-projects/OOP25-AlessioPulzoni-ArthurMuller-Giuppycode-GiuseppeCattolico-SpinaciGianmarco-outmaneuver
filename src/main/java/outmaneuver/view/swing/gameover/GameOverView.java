package outmaneuver.view.swing.gameover;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import outmaneuver.model.session.ScoreEntry;
import outmaneuver.view.swing.Theme;
import outmaneuver.view.swing.leaderboard.LeaderboardTablePanel;

public final class GameOverView extends JPanel {

    private static final int TITLE_FONT_SIZE   = 64;
    private static final int SCORE_FONT_SIZE   = 28;
    private static final int RECAP_FONT_SIZE   = 22;
    private static final int VGAP              = 14;

    private final JLabel scoreLabel;
    private final JLabel recapStarsLabel;
    private final JLabel recapMissilesLabel;
    private final LeaderboardTablePanel tablePanel;

    public GameOverView(final Runnable onPlayAgain, final Runnable onMenu) {
        final Runnable safePlayAgain = Objects.requireNonNull(onPlayAgain);
        final Runnable safeMenu      = Objects.requireNonNull(onMenu);

        setBackground(Theme.BACKGROUND);
        setLayout(new GridBagLayout());

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx  = 0;
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(VGAP, 0, 0, 0);

        final JLabel title = Theme.outlinedLabel("GAME OVER", new Font(Font.SANS_SERIF, Font.BOLD, TITLE_FONT_SIZE), Theme.TEXT_ERROR);

        scoreLabel = Theme.outlinedLabel("Score: 0", new Font(Font.MONOSPACED, Font.BOLD, SCORE_FONT_SIZE), Theme.TEXT_TITLE);

        final var recapFont = new Font(Font.MONOSPACED, Font.BOLD, RECAP_FONT_SIZE);
        recapStarsLabel    = Theme.outlinedLabel("Stelle collezionate + 0", recapFont, Theme.TEXT_TITLE);
        recapMissilesLabel = Theme.outlinedLabel("Missili fatti scontrare + 0", recapFont, Theme.TEXT_TITLE);

        tablePanel = new LeaderboardTablePanel(5);

        final JButton playAgainButton = Theme.styledButton("PLAY AGAIN", Theme.FONT_BUTTON, Theme.BUTTON_WIDTH, Theme.BUTTON_HEIGHT);
        final JButton menuButton      = Theme.styledButton("MENU",       Theme.FONT_BUTTON, Theme.BUTTON_WIDTH, Theme.BUTTON_HEIGHT);
        playAgainButton.addActionListener(e -> safePlayAgain.run());
        menuButton.addActionListener(e -> safeMenu.run());

        gbc.gridy = 0; add(title,              gbc);
        gbc.gridy = 1; add(scoreLabel,          gbc);
        gbc.gridy = 2; add(recapStarsLabel,     gbc);
        gbc.gridy = 3; add(recapMissilesLabel,  gbc);
        gbc.gridy = 4; add(tablePanel,          gbc);
        gbc.gridy = 5; add(playAgainButton,     gbc);
        gbc.gridy = 6; add(menuButton,          gbc);
    }

    /**
     * Aggiorna il contenuto della schermata con i dati della partita appena conclusa.
     * Deve essere chiamato sull'EDT, subito prima di mostrare questa schermata.
     */
    public void show(final int finalScore, final List<ScoreEntry> topScores, final int starsScore, final int missilesScore) {
        Objects.requireNonNull(topScores, "topScores must not be null");
        SwingUtilities.invokeLater(() -> {
            scoreLabel.setText("Score: " + finalScore);
            recapStarsLabel.setText("Stelle collezionate + " + starsScore + " punti");
            recapMissilesLabel.setText("Missili scontrati + " + missilesScore + " punti");
            tablePanel.refresh(topScores);
            revalidate();
            repaint();
        });
    }
}

