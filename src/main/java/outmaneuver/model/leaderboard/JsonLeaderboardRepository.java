package outmaneuver.model.leaderboard;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import outmaneuver.model.session.ScoreEntry;

public final class JsonLeaderboardRepository implements ILeaderboardRepository {

    private static final Type SCORE_LIST_TYPE = new TypeToken<List<ScoreEntry>>() { }.getType();

    private final Path filePath;
    private final Gson gson;

    public JsonLeaderboardRepository(final Path filePath) {
        this.filePath = Objects.requireNonNull(filePath, "filePath must not be null");
        this.gson = buildGson();
    }

    @Override
    public List<ScoreEntry> load() {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }
        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            final List<ScoreEntry> result = gson.fromJson(reader, SCORE_LIST_TYPE);
            return result != null ? result : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void persist(final List<ScoreEntry> entries) {
        Objects.requireNonNull(entries, "entries must not be null");
        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                gson.toJson(entries, SCORE_LIST_TYPE, writer);
            }
        } catch (IOException e) {
            throw new LeaderboardIOException("Failed to persist leaderboard to " + filePath, e);
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
