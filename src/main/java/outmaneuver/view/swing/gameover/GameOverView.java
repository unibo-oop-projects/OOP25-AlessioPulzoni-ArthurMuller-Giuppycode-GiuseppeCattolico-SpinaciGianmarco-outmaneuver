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

import outmaneuver.assembler.ScreenAssembler.ScreenMetrics;
import outmaneuver.model.session.ScoreEntry;
import outmaneuver.view.swing.Theme;
import outmaneuver.view.swing.leaderboard.LeaderboardTablePanel;

/**
 * Swing screen shown when a run ends: displays the final score, the stars/missiles
 * recap, a leaderboard excerpt and buttons to play again or return to the menu.
 */
public final class GameOverView extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int TITLE_INSET = 14;
    private static final int SCORE_FONT_SIZE = 28;
    private static final int RECAP_LEADERBOARD_MAX_ENTRIES = 5;
    private static final int PLAY_AGAIN_ROW_GRIDY = 5;
    private static final int MENU_ROW_GRIDY = 6;

    private final JLabel scoreLabel;
    private final JLabel recapStarsLabel;
    private final JLabel recapMissilesLabel;
    private final LeaderboardTablePanel tablePanel;

    /**
     * Creates the game-over screen.
     *
     * @param metrics scaling metrics for the current window size
     * @param onPlayAgain callback invoked when the player clicks "Play again"
     * @param onMenu callback invoked when the player clicks "Menu"
     */
    public GameOverView(final ScreenMetrics metrics, final Runnable onPlayAgain, final Runnable onMenu) {
        final Runnable safePlayAgain = Objects.requireNonNull(onPlayAgain);
        final Runnable safeMenu = Objects.requireNonNull(onMenu);

        setBackground(Theme.BACKGROUND);
        setLayout(new GridBagLayout());

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(metrics.sh(TITLE_INSET), 0, 0, 0);

        final JLabel title = Theme.outlinedLabel("GAME OVER",
                new Font(Font.SANS_SERIF, Font.BOLD, metrics.sf(64)), Theme.TEXT_ERROR);

        scoreLabel = Theme.outlinedLabel("Score: 0",
                new Font(Font.MONOSPACED, Font.BOLD, metrics.sf(SCORE_FONT_SIZE)), Theme.TEXT_TITLE);

        final var recapFont = new Font(Font.MONOSPACED, Font.BOLD, metrics.sf(22));
        recapStarsLabel = Theme.outlinedLabel("Stelle collezionate + 0", recapFont, Theme.TEXT_TITLE);
        recapMissilesLabel = Theme.outlinedLabel("Missili fatti scontrare + 0", recapFont, Theme.TEXT_TITLE);

        tablePanel = new LeaderboardTablePanel(metrics, RECAP_LEADERBOARD_MAX_ENTRIES);

        final JButton playAgainButton = Theme.styledButton("PLAY AGAIN", metrics.sf(Theme.FONT_BUTTON),
                metrics.sw(Theme.BUTTON_WIDTH), metrics.sh(Theme.BUTTON_HEIGHT));
        final JButton menuButton = Theme.styledButton("MENU", metrics.sf(Theme.FONT_BUTTON),
                metrics.sw(Theme.BUTTON_WIDTH), metrics.sh(Theme.BUTTON_HEIGHT));
        playAgainButton.addActionListener(e -> safePlayAgain.run());
        menuButton.addActionListener(e -> safeMenu.run());

        gbc.gridy = 0;
        add(title, gbc);
        gbc.gridy = 1;
        add(scoreLabel, gbc);
        gbc.gridy = 2;
        add(recapStarsLabel, gbc);
        gbc.gridy = 3;
        add(recapMissilesLabel, gbc);
        gbc.gridy = 4;
        add(tablePanel, gbc);
        gbc.gridy = PLAY_AGAIN_ROW_GRIDY;
        add(playAgainButton, gbc);
        gbc.gridy = MENU_ROW_GRIDY;
        add(menuButton, gbc);
    }

    /**
     * Aggiorna il contenuto della schermata con i dati della partita appena conclusa.
     * Deve essere chiamato sull'EDT, subito prima di mostrare questa schermata.
     *
     * @param finalScore the final score of the run
     * @param topScores the current leaderboard entries to display in the recap table
     * @param starsScore points earned from collected stars
     * @param missilesScore points earned from missiles destroyed/avoided
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

