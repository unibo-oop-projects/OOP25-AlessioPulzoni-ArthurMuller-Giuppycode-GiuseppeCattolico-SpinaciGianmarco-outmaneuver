package outmaneuver.model.profile;

/** Storage backend for loading and persisting {@link PlayerProfileData}. */
public interface IPlayerProfileRepository {

    /**
     * Carica il profilo salvato. Restituisce {@link PlayerProfileData#defaultProfile()} se assente.
     *
     * @return the loaded profile data, or the default profile if none was saved
     */
    PlayerProfileData load();

    /**
     * Persists the given profile data.
     *
     * @param data the profile data to persist
     */
    void persist(PlayerProfileData data);
}
