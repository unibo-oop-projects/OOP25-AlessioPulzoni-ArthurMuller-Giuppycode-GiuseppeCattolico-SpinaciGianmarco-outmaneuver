package outmaneuver.model.profile;

import java.util.List;
import java.util.Objects;

import outmaneuver.model.session.ScoreEntry;

/**
 * Snapshot immutabile di tutti i dati persistenti del giocatore.
 * Usato solo come DTO tra {@link IPlayerProfileRepository} e {@link PlayerProfile}.
 *
 * @param playerName the player's display name
 * @param coins the number of coins owned
 * @param ownedPlaneIds the ids of the planes owned by the player
 * @param scores the player's leaderboard entries
 */
public record PlayerProfileData(
        String playerName,
        int coins,
        List<String> ownedPlaneIds,
        List<ScoreEntry> scores
) {
    /** Validates the data and defensively copies the mutable collections. */
    public PlayerProfileData {
        Objects.requireNonNull(playerName, "playerName must not be null");
        Objects.requireNonNull(ownedPlaneIds, "ownedPlaneIds must not be null");
        Objects.requireNonNull(scores, "scores must not be null");
        if (coins < 0) {
            throw new IllegalArgumentException("coins must not be negative");
        }
        ownedPlaneIds = List.copyOf(ownedPlaneIds);
        scores = List.copyOf(scores);
    }

    /** Profilo di default per un nuovo giocatore: standard già posseduto. */
    public static PlayerProfileData defaultProfile() {
        return new PlayerProfileData("DefaultPlayerName", 0, List.of("standard"), List.of());
    }
}
