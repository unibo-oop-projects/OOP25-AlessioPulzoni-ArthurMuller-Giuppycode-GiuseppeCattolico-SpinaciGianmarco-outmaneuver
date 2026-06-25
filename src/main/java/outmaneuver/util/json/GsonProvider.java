package outmaneuver.util.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/** Centralises construction of the {@link Gson} instances used across the codebase. */
public final class GsonProvider {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private GsonProvider() {
    }

    /**
     * Returns a shared, pretty-printing {@link Gson} instance with default settings.
     *
     * @return a ready-to-use {@link Gson} instance
     */
    public static Gson create() {
        return GSON;
    }

    /**
     * Returns a new pretty-printing {@link GsonBuilder}, for callers that need
     * to register additional type adapters before building their own {@link Gson}.
     *
     * @return a new, pre-configured {@link GsonBuilder}
     */
    public static GsonBuilder builder() {
        return new GsonBuilder().setPrettyPrinting();
    }
}
