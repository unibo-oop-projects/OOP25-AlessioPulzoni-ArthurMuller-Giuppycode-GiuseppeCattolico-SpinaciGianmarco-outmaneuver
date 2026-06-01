package outmaneuver;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import outmaneuver.model.session.GameState;
import outmaneuver.view.UIManager;
import outmaneuver.view.gameover.GameOverView;
import outmaneuver.view.menu.MainMenuView;

// TODO: classe temporanea per testing visuale — da sostituire con il controller reale
public final class Main {

    private Main() { }

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(() -> {
            final JFrame frame = new JFrame("OutManeuver");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);

            final GameOverView gameOverView = new GameOverView();
            final MainMenuView mainMenuView = new MainMenuView(
                () -> System.out.println("START pressed — controller non ancora collegato"),
                () -> System.exit(0)
            );
            final UIManager uiManager = new UIManager(mainMenuView, gameOverView);
            uiManager.showScreen(GameState.MENU);

            frame.setContentPane(uiManager);
            frame.setVisible(true);
        });
    }
}
