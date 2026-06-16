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

public final class JsonResourceLoader<T> {

    private final String resourcePath;
    private final Type type;
    private final Gson gson;

    private JsonResourceLoader(final String resourcePath, final Type type, final Gson gson) {
        this.resourcePath = Objects.requireNonNull(resourcePath, "resourcePath must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.gson = Objects.requireNonNull(gson, "gson must not be null");
    }

    public static <T> JsonResourceLoader<T> forType(
            final String resourcePath, final Type type, final Gson gson) {
        return new JsonResourceLoader<>(resourcePath, type, gson);
    }

    public static <T> JsonResourceLoader<List<T>> forList(
            final String resourcePath, final Class<T> elementType, final Gson gson) {
        final Type listType = TypeToken.getParameterized(List.class, elementType).getType();
        return new JsonResourceLoader<>(resourcePath, listType, gson);
    }

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
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read resource: " + resourcePath, e);
        }
    }
}
