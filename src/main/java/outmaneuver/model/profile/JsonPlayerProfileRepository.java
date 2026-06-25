package outmaneuver.model.profile;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import outmaneuver.util.json.GsonProvider;
import outmaneuver.util.json.JsonFileStore;

/** {@link IPlayerProfileRepository} backed by a JSON file on disk. */
public final class JsonPlayerProfileRepository implements IPlayerProfileRepository {

    private static final String PROFILE_FILE = "profile.json";
    private static final String PROFILE_DIR = ".outmaneuver";

    private final JsonFileStore<PlayerProfileData> store;

    /**
     * Creates a repository backed by the given file store.
     *
     * @param store the JSON file store to read from and write to
     */
    public JsonPlayerProfileRepository(final JsonFileStore<PlayerProfileData> store) {
        this.store = Objects.requireNonNull(store, "store must not be null");
    }

    /**
     * Restituisce il path predefinito per il file di profilo in base alla piattaforma.
     * <ul>
     *   <li>Windows → {@code %LOCALAPPDATA%\.outmaneuver\profile.json}
     *   <li>altri OS → {@code ~/.outmaneuver/profile.json}
     * </ul>
     *
     * @return il path predefinito del file di profilo
     */
    public static Path defaultProfilePath() {
        if (isWindows()) {
            return Path.of(System.getenv("LOCALAPPDATA"), PROFILE_DIR, PROFILE_FILE);
        }
        return Path.of(System.getProperty("user.home"), PROFILE_DIR, PROFILE_FILE);
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
    }

    /**
     * Factory method: crea un repository configurato con il percorso predefinito
     * (vedi {@link #defaultProfilePath()}).
     *
     * @return un nuovo repository sul path predefinito
     */
    public static JsonPlayerProfileRepository create() {
        return create(defaultProfilePath());
    }

    /**
     * Factory method: crea un repository configurato con il path del file utente.
     * Usa un {@link Gson} con supporto a {@link java.time.LocalDate}.
     *
     * @param filePath il path del file JSON da usare per la persistenza
     * @return un nuovo repository configurato su {@code filePath}
     */
    public static JsonPlayerProfileRepository create(final Path filePath) {
        Objects.requireNonNull(filePath, "filePath must not be null");
        final Gson gson = GsonProvider.builder()
                .registerTypeAdapter(LocalDate.class,
                        (JsonSerializer<LocalDate>) (src, t, ctx) -> new JsonPrimitive(src.toString()))
                .registerTypeAdapter(LocalDate.class,
                        (JsonDeserializer<LocalDate>) (json, t, ctx) -> LocalDate.parse(json.getAsString()))
                .create();
        return new JsonPlayerProfileRepository(
                JsonFileStore.forType(filePath, PlayerProfileData.class, gson));
    }

    @Override
    public PlayerProfileData load() {
        final PlayerProfileData data = store.load();
        return data != null ? data : PlayerProfileData.defaultProfile();
    }

    @Override
    public void persist(final PlayerProfileData data) {
        Objects.requireNonNull(data, "data must not be null");
        store.save(data);
    }
}
