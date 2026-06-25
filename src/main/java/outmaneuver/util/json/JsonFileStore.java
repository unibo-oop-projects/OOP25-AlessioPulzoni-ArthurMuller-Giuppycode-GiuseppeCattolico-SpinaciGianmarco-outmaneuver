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

/**
 * Reads and writes a single JSON file holding a value of type {@code T}, using Gson.
 *
 * @param <T> the type of the value stored in the file
 */
public final class JsonFileStore<T> {

    private final Path filePath;
    private final Type type;
    private final Gson gson;

    private JsonFileStore(final Path filePath, final Type type, final Gson gson) {
        this.filePath = Objects.requireNonNull(filePath, "filePath must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.gson = Objects.requireNonNull(gson, "gson must not be null");
    }

    /**
     * Creates a store for a value of the given type.
     *
     * @param <T> the type of the stored value
     * @param filePath the JSON file to read from and write to
     * @param type the runtime type of the stored value
     * @param gson the Gson instance to use for (de)serialization
     * @return a new store configured for {@code type}
     */
    public static <T> JsonFileStore<T> forType(
            final Path filePath, final Type type, final Gson gson) {
        return new JsonFileStore<>(filePath, type, gson);
    }

    /**
     * Creates a store for a list of elements of the given type.
     *
     * @param <T> the element type of the stored list
     * @param filePath the JSON file to read from and write to
     * @param elementType the class of each element in the list
     * @param gson the Gson instance to use for (de)serialization
     * @return a new store configured for a {@code List<T>}
     */
    public static <T> JsonFileStore<List<T>> forList(
            final Path filePath, final Class<T> elementType, final Gson gson) {
        final Type listType = TypeToken.getParameterized(List.class, elementType).getType();
        return new JsonFileStore<>(filePath, listType, gson);
    }

    /**
     * Loads the value stored in the file.
     *
     * @return the deserialized value, or {@code null} if the file does not exist
     */
    public T load() {
        if (!Files.exists(filePath)) {
            return null;
        }
        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, type);
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to read file: " + filePath, e);
        }
    }

    /**
     * Serializes and writes the given value to the file, creating parent directories if needed.
     *
     * @param data the value to persist
     */
    public void save(final T data) {
        Objects.requireNonNull(data, "data must not be null");
        try {
            final Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (Writer writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                gson.toJson(data, type, writer);
            }
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to write file: " + filePath, e);
        }
    }
}
