package outmaneuver.util.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class GsonProvider {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private GsonProvider() {
    }

    public static Gson create() {
        return GSON;
    }

    public static GsonBuilder builder() {
        return new GsonBuilder().setPrettyPrinting();
    }
}
