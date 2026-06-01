package outmaneuver.view.swing;

import java.awt.CardLayout;
import java.util.Objects;

import javax.swing.JPanel;

import outmaneuver.model.session.GameState;
import outmaneuver.view.swing.gameover.GameOverView;
import outmaneuver.view.swing.menu.MainMenuView;

public class UIManager extends JPanel {

    private static final String SCREEN_MENU      = "MENU";
    private static final String SCREEN_PLAYING   = "PLAYING";
    private static final String SCREEN_GAME_OVER = "GAME_OVER";

    private final CardLayout cardLayout;

    public UIManager(final MainMenuView mainMenuView,
                     final GameOverView gameOverView,
                     final JPanel gamePanel) {
        Objects.requireNonNull(mainMenuView);
        Objects.requireNonNull(gameOverView);
        Objects.requireNonNull(gamePanel);

        this.cardLayout = new CardLayout();
        setLayout(cardLayout);

        add(mainMenuView, SCREEN_MENU);
        add(gamePanel,    SCREEN_PLAYING);
        add(gameOverView, SCREEN_GAME_OVER);
    }

    public void showScreen(final GameState state) {
        final String card = switch (state) {
            case MENU               -> SCREEN_MENU;
            case PLAYING, PAUSED   -> SCREEN_PLAYING;
            case GAME_OVER          -> SCREEN_GAME_OVER;
        };
        cardLayout.show(this, card);
    }
}
