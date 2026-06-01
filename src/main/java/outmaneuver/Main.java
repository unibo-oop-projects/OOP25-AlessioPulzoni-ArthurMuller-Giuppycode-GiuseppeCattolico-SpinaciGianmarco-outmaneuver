package outmaneuver;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import outmaneuver.view.menu.MainMenuView;

// TODO: classe temporanea per testing visuale — da sostituire con il controller reale
public class Main extends Application {

    @Override
    public void start(final Stage primaryStage) {
        final MainMenuView menuView = new MainMenuView(
            () -> System.out.println("START pressed — controller non ancora collegato"),
            Platform::exit
        );

        primaryStage.setScene(new Scene(menuView, 800, 600));
        primaryStage.setTitle("OutManeuver");
        primaryStage.show();
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
