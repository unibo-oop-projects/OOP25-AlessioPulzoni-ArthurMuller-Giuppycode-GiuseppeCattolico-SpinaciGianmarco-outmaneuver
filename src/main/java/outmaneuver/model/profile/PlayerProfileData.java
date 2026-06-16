package outmaneuver.model.profile;

import java.util.List;
import java.util.Objects;

import outmaneuver.model.session.ScoreEntry;

/**
 * Snapshot immutabile di tutti i dati persistenti del giocatore.
 * Usato solo come DTO tra {@link IPlayerProfileRepository} e {@link PlayerProfile}.
 */
public record PlayerProfileData(
        String playerName,
        int coins,
        List<String> ownedPlaneIds,
        List<ScoreEntry> scores
) {
    public PlayerProfileData {
        Objects.requireNonNull(playerName, "playerName must not be null");
        Objects.requireNonNull(ownedPlaneIds, "ownedPlaneIds must not be null");
        Objects.requireNonNull(scores, "scores must not be null");
        if (coins < 0) {
            throw new IllegalArgumentException("coins must not be negative");
        }
        ownedPlaneIds = List.copyOf(ownedPlaneIds);
        scores        = List.copyOf(scores);
    }

    /** Profilo di default per un nuovo giocatore: standard già posseduto. */
    public static PlayerProfileData defaultProfile() {
        return new PlayerProfileData("DefaultPlayerName", 1000, List.of("standard"), List.of());
    }
}
