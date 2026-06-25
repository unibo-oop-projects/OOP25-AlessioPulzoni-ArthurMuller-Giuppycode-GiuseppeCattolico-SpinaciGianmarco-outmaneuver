package outmaneuver.util.json;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Reads a single read-only JSON resource from the classpath, deserializing it
 * into a value of type {@code T} using Gson.
 *
 * @param <T> the type of the value read from the resource
 */
public final class JsonResourceLoader<T> {

    private final String resourcePath;
    private final Type type;
    private final Gson gson;

    private JsonResourceLoader(final String resourcePath, final Type type, final Gson gson) {
        this.resourcePath = Objects.requireNonNull(resourcePath, "resourcePath must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.gson = Objects.requireNonNull(gson, "gson must not be null");
    }

    /**
     * Creates a loader for a value of the given type.
     *
     * @param <T> the type of the loaded value
     * @param resourcePath the classpath location of the JSON resource
     * @param type the runtime type of the loaded value
     * @param gson the Gson instance to use for deserialization
     * @return a new loader configured for {@code type}
     */
    public static <T> JsonResourceLoader<T> forType(
            final String resourcePath, final Type type, final Gson gson) {
        return new JsonResourceLoader<>(resourcePath, type, gson);
    }

    /**
     * Creates a loader for a list of elements of the given type.
     *
     * @param <T> the element type of the loaded list
     * @param resourcePath the classpath location of the JSON resource
     * @param elementType the class of each element in the list
     * @param gson the Gson instance to use for deserialization
     * @return a new loader configured for a {@code List<T>}
     */
    public static <T> JsonResourceLoader<List<T>> forList(
            final String resourcePath, final Class<T> elementType, final Gson gson) {
        final Type listType = TypeToken.getParameterized(List.class, elementType).getType();
        return new JsonResourceLoader<>(resourcePath, listType, gson);
    }

    /**
     * Loads and deserializes the resource.
     *
     * @return the deserialized value
     */
    public T load() {
        final var stream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new UncheckedIOException(
                    new IOException("Resource not found: " + resourcePath));
        }
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            final T data = gson.fromJson(reader, type);
            if (data == null) {
                throw new UncheckedIOException(
                        new IOException("Resource is empty or malformed: " + resourcePath));
            }
            return data;
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to read resource: " + resourcePath, e);
        }
    }
}
