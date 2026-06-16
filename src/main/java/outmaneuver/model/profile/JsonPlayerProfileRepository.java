package outmaneuver.model.profile;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import outmaneuver.util.json.GsonProvider;
import outmaneuver.util.json.JsonFileStore;

public final class JsonPlayerProfileRepository implements IPlayerProfileRepository {

    private final JsonFileStore<PlayerProfileData> store;

    public JsonPlayerProfileRepository(final JsonFileStore<PlayerProfileData> store) {
        this.store = Objects.requireNonNull(store, "store must not be null");
    }

    /**
     * Factory method: crea un repository configurato con il path del file utente.
     * Usa un {@link Gson} con supporto a {@link java.time.LocalDate}.
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
