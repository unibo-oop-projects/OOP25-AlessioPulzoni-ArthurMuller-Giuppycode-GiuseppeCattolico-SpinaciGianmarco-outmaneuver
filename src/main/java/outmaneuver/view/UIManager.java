package outmaneuver.view;

import java.awt.CardLayout;
import java.util.Objects;

import javax.swing.JPanel;

import outmaneuver.model.session.GameState;
import outmaneuver.view.gameover.GameOverView;
import outmaneuver.view.menu.MainMenuView;

public class UIManager extends JPanel {

    private static final String SCREEN_MENU      = "MENU";
    private static final String SCREEN_GAME_OVER = "GAME_OVER";

    private final CardLayout cardLayout;

    public UIManager(final MainMenuView mainMenuView, final GameOverView gameOverView) {
        Objects.requireNonNull(mainMenuView);
        Objects.requireNonNull(gameOverView);

        this.cardLayout = new CardLayout();
        setLayout(cardLayout);

        add(mainMenuView, SCREEN_MENU);
        add(gameOverView, SCREEN_GAME_OVER);
    }

    public void showScreen(final GameState state) {
        final String card = switch (state) {
            case MENU      -> SCREEN_MENU;
            case GAME_OVER -> SCREEN_GAME_OVER;
            default        -> SCREEN_MENU;
        };
        cardLayout.show(this, card);
    }
}
