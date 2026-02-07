package eu.ha3.presencefootsteps.world;

import java.io.Reader;
import java.util.function.Consumer;
import net.minecraft.resources.Identifier;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import eu.ha3.presencefootsteps.util.BlockReport.Reportable;

public interface Index<K, V> extends Reportable {
    /**
     * Finds the mapped result for a given key.
     */
    V lookup(K key);

    /**
     * Checks whether the given key identifier contains a mapping in this index.
     */
    boolean contains(Identifier key);

    /**
     * Creates a resource loader to populate this index.
     */
    Loader createLoader();

    interface Loader extends Consumer<Reader> {
        Gson GSON = new Gson();

        @Override
        default void accept(Reader reader) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);

            json.entrySet().forEach(entry -> {
                accept(entry.getKey(), entry.getValue());
            });
        }

        void accept(String key, JsonElement json);
    }
}
