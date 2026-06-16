package outmaneuver.model.profile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import outmaneuver.model.session.ScoreEntry;
import outmaneuver.model.wallet.IWallet;

/**
 * Aggregato di tutti i dati persistenti del giocatore.
 * Implementa {@link IWallet} per integrarsi con {@code Shop.purchase()}.
 */
public final class PlayerProfile implements IWallet {

    private static final int MAX_SCORES = 10;

    private final IPlayerProfileRepository repository;
    private String playerName;
    private int coins;
    private final Set<String> ownedPlaneIds;
    private final List<ScoreEntry> scores;

    public PlayerProfile(final IPlayerProfileRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        final PlayerProfileData data = repository.load();
        this.playerName   = data.playerName();
        this.coins        = data.coins();
        this.ownedPlaneIds = new LinkedHashSet<>(data.ownedPlaneIds());
        this.scores       = new ArrayList<>(data.scores());
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(final String name) {
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        this.playerName = name;
        save();
    }

    // ── IWallet ──────────────────────────────────────────────────────────

    @Override
    public int getCoins() {
        return coins;
    }

    @Override
    public void addCoins(final int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive, was: " + amount);
        }
        coins += amount;
        save();
    }

    @Override
    public boolean spend(final int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive, was: " + amount);
        }
        if (coins < amount) {
            return false;
        }
        coins -= amount;
        save();
        return true;
    }

    // ── Owned planes ─────────────────────────────────────────────────────

    public boolean ownsPlane(final String planeId) {
        return ownedPlaneIds.contains(Objects.requireNonNull(planeId));
    }

    public void addOwnedPlane(final String planeId) {
        Objects.requireNonNull(planeId, "planeId must not be null");
        if (ownedPlaneIds.add(planeId)) {
            save();
        }
    }

    // ── Leaderboard ───────────────────────────────────────────────────────

    public void saveScore(final int score, final String name) {
        Objects.requireNonNull(name, "name must not be null");
        scores.add(new ScoreEntry(score, name, LocalDate.now()));
        Collections.sort(scores);
        while (scores.size() > MAX_SCORES) {
            scores.remove(scores.size() - 1);
        }
        save();
    }

    public List<ScoreEntry> getTopScores() {
        return Collections.unmodifiableList(scores);
    }

    // ── Persistence ───────────────────────────────────────────────────────

    private void save() {
        repository.persist(new PlayerProfileData(
                playerName,
                coins,
                List.copyOf(ownedPlaneIds),
                List.copyOf(scores)
        ));
    }
}
