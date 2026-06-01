package outmaneuver.view.menu;

import java.util.Objects;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class MainMenuView extends StackPane {

    public MainMenuView(final Runnable onStart, final Runnable onExit) {
        final Runnable safeStart = Objects.requireNonNull(onStart);
        final Runnable safeExit  = Objects.requireNonNull(onExit);

        setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        final Label title = new Label("OUTMANEUVER");
        title.setFont(Font.font("System", FontWeight.BOLD, 64));
        title.setTextFill(Color.WHITE);

        final Button startButton = new Button("START");
        final Button exitButton = new Button("EXIT");

        startButton.setOnAction(e -> safeStart.run());
        exitButton.setOnAction(e -> safeExit.run());

        final VBox layout = new VBox(20, title, startButton, exitButton);
        layout.setAlignment(Pos.CENTER);

        getChildren().add(layout);
    }
}
