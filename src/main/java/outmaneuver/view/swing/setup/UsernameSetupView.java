package outmaneuver.view.swing.setup;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import outmaneuver.view.swing.Theme;

public final class UsernameSetupView extends JPanel {

    private static final int TITLE_FONT_SIZE  = 40;
    private static final int LABEL_FONT_SIZE  = 18;
    private static final int FIELD_COLUMNS    = 20;
    private static final int VGAP             = 16;
    private static final int MAX_NAME_LENGTH  = 24;

    public UsernameSetupView(final Consumer<String> onConfirm) {
        Objects.requireNonNull(onConfirm, "onConfirm must not be null");

        setBackground(Theme.BACKGROUND);
        setLayout(new GridBagLayout());

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx  = 0;
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(VGAP, 0, 0, 0);

        final JLabel title = Theme.outlinedLabel("WELCOME", new Font(Font.SANS_SERIF, Font.BOLD, TITLE_FONT_SIZE), Theme.TEXT_TITLE);

        final JLabel prompt = Theme.outlinedLabel("Enter your username:", new Font(Font.MONOSPACED, Font.PLAIN, LABEL_FONT_SIZE), Theme.TEXT_BODY);

        final JTextField nameField = new JTextField(FIELD_COLUMNS);
        nameField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, LABEL_FONT_SIZE));
        nameField.setHorizontalAlignment(JTextField.CENTER);

        final JLabel errorLabel = Theme.outlinedLabel(" ", new Font(Font.MONOSPACED, Font.PLAIN, LABEL_FONT_SIZE - 4), Theme.TEXT_ERROR);

        final JButton confirmButton = Theme.styledButton("CONFIRM", Theme.FONT_BUTTON, Theme.BUTTON_WIDTH, Theme.BUTTON_HEIGHT);

        final Runnable doConfirm = () -> {
            final String name = nameField.getText().strip();
            if (name.isEmpty()) {
                errorLabel.setText("Username cannot be empty.");
                return;
            }
            if (name.length() > MAX_NAME_LENGTH) {
                errorLabel.setText("Username is too long (max " + MAX_NAME_LENGTH + " chars).");
                return;
            }
            onConfirm.accept(name);
        };

        confirmButton.addActionListener(e -> doConfirm.run());
        nameField.addActionListener(e -> doConfirm.run()); // confirm on Enter

        gbc.gridy = 0; add(title,         gbc);
        gbc.gridy = 1; add(prompt,        gbc);
        gbc.gridy = 2; add(nameField,     gbc);
        gbc.gridy = 3; add(errorLabel,    gbc);
        gbc.gridy = 4; add(confirmButton, gbc);
    }
}
