package outmaneuver.model.session;

import java.time.LocalDate;
import java.util.Objects;

public record ScoreEntry(int score, String playerName, LocalDate date)
        implements Comparable<ScoreEntry> {

    public ScoreEntry {
        Objects.requireNonNull(playerName);
        Objects.requireNonNull(date);
    }

    @Override
    public int compareTo(final ScoreEntry other) {
        return Integer.compare(other.score(), this.score());
    }
}
