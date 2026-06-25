package outmaneuver;

import javax.swing.SwingUtilities;

/** Application entry point. */
public final class Main {

    private Main() {
    }

    /**
     * Starts the application on the Swing event dispatch thread.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(AppBootstrapper::launch);
    }
}

