package outmaneuver.util.json;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public final class JsonFileStore<T> {

    private final Path filePath;
    private final Type type;
    private final Gson gson;

    private JsonFileStore(final Path filePath, final Type type, final Gson gson) {
        this.filePath = Objects.requireNonNull(filePath, "filePath must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.gson = Objects.requireNonNull(gson, "gson must not be null");
    }

    public static <T> JsonFileStore<T> forType(
            final Path filePath, final Type type, final Gson gson) {
        return new JsonFileStore<>(filePath, type, gson);
    }

    public static <T> JsonFileStore<List<T>> forList(
            final Path filePath, final Class<T> elementType, final Gson gson) {
        final Type listType = TypeToken.getParameterized(List.class, elementType).getType();
        return new JsonFileStore<>(filePath, listType, gson);
    }

    public T load() {
        if (!Files.exists(filePath)) {
            return null;
        }
        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read file: " + filePath, e);
        }
    }

    public void save(final T data) {
        Objects.requireNonNull(data, "data must not be null");
        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                gson.toJson(data, type, writer);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write file: " + filePath, e);
        }
    }
}
