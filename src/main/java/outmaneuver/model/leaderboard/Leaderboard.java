package outmaneuver.model.leaderboard;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import outmaneuver.model.session.ScoreEntry;

public final class Leaderboard {

    private static final int DEFAULT_MAX_ENTRIES = 10;

    private final ILeaderboardRepository repository;
    private final int maxEntries;

    public Leaderboard(final ILeaderboardRepository repository) {
        this(repository, DEFAULT_MAX_ENTRIES);
    }

    public Leaderboard(final ILeaderboardRepository repository, final int maxEntries) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        if (maxEntries <= 0) {
            throw new IllegalArgumentException("maxEntries must be positive");
        }
        this.maxEntries = maxEntries;
    }

    public void save(final int score, final String playerName) {
        Objects.requireNonNull(playerName, "playerName must not be null");
        final List<ScoreEntry> entries = new ArrayList<>(repository.load());
        entries.add(new ScoreEntry(score, playerName, LocalDate.now()));
        Collections.sort(entries);
        final List<ScoreEntry> trimmed = entries.size() > maxEntries
                ? entries.subList(0, maxEntries)
                : entries;
        repository.persist(new ArrayList<>(trimmed));
    }

    public List<ScoreEntry> getTopScores() {
        return Collections.unmodifiableList(repository.load());
    }
}
