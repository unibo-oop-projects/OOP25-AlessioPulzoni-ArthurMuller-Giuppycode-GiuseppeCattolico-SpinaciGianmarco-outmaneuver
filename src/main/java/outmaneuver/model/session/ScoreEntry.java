package outmaneuver.model.session;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A single leaderboard entry, sortable by score (highest first).
 *
 * @param score the achieved score
 * @param playerName the name of the player who achieved it
 * @param date the date the score was achieved
 */
public record ScoreEntry(int score, String playerName, LocalDate date)
        implements Comparable<ScoreEntry> {

    /** Validates that the entry has a non-null player name and date. */
    public ScoreEntry {
        Objects.requireNonNull(playerName);
        Objects.requireNonNull(date);
    }

    @Override
    public int compareTo(final ScoreEntry other) {
        return Integer.compare(other.score(), this.score());
    }
}
