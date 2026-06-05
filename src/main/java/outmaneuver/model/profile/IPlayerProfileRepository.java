package outmaneuver.model.profile;

public interface IPlayerProfileRepository {

    /** Carica il profilo salvato. Restituisce {@link PlayerProfileData#defaultProfile()} se assente. */
    PlayerProfileData load();

    void persist(PlayerProfileData data);
}
