package outmaneuver.model.profile;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

public final class JsonPlayerProfileRepository implements IPlayerProfileRepository {

    private static final Type PROFILE_TYPE = new TypeToken<PlayerProfileData>() { }.getType();

    private final Path filePath;
    private final Gson gson;

    public JsonPlayerProfileRepository(final Path filePath) {
        this.filePath = Objects.requireNonNull(filePath, "filePath must not be null");
        this.gson = buildGson();
    }

    @Override
    public PlayerProfileData load() {
        if (!Files.exists(filePath)) {
            return PlayerProfileData.defaultProfile();
        }
        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            final PlayerProfileData data = gson.fromJson(reader, PROFILE_TYPE);
            return data != null ? data : PlayerProfileData.defaultProfile();
        } catch (IOException e) {
            return PlayerProfileData.defaultProfile();
        }
    }

    @Override
    public void persist(final PlayerProfileData data) {
        Objects.requireNonNull(data, "data must not be null");
        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                gson.toJson(data, PROFILE_TYPE, writer);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to persist player profile to " + filePath, e);
        }
    }

    private static Gson buildGson() {
        final JsonSerializer<LocalDate> serializer =
                (src, typeOfSrc, context) -> context.serialize(src.toString());
        final JsonDeserializer<LocalDate> deserializer =
                (json, typeOfT, context) -> LocalDate.parse(json.getAsString());
        return new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, serializer)
                .registerTypeAdapter(LocalDate.class, deserializer)
                .setPrettyPrinting()
                .create();
    }
}
