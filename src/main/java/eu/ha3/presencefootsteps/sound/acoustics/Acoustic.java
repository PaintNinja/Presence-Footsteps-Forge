package eu.ha3.presencefootsteps.sound.acoustics;

import java.io.IOException;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.world.entity.LivingEntity;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.sound.player.SoundPlayer;
import eu.ha3.presencefootsteps.util.JsonObjectWriter;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * Something that has the ability to play sounds.
 *
 * @author Hurry
 */
public interface Acoustic {
    Map<String, Serializer> FACTORIES = Object2ObjectMaps.unmodifiable(new Object2ObjectOpenHashMap<>(new String[] {
            "basic",
            "events",
            "simultaneous",
            "delayed",
            "probability",
            "chance"
    }, new Serializer[] {
            VaryingAcoustic.FACTORY,        // basic
            EventSelectorAcoustics.FACTORY, // events
            SimultaneousAcoustic.FACTORY,   // simultaneous
            DelayedAcoustic.FACTORY,        // delayed
            WeightedAcoustic.FACTORY,       // probability
            ChanceAcoustic.FACTORY          // chance
    }));

    static Acoustic read(AcousticsFile context, JsonElement unsolved) throws JsonParseException {
        return read(context, unsolved, "basic");
    }

    static Acoustic read(AcousticsFile context, JsonElement json, String defaultUnassigned) throws JsonParseException {
        String type = getType(json, defaultUnassigned);
        return checked(checked(FACTORIES.get(type), () -> "Invalid type for acoustic `" + type + "`").create(json, context), () -> "Unresolved Json element: \r\n" + json.toString());
    }

    private static String getType(JsonElement unsolved, String defaultUnassigned) {
        if (unsolved.isJsonObject()) {
            JsonObject json = unsolved.getAsJsonObject();
            return json.has("type") ? json.get("type").getAsString() : defaultUnassigned;
        }

        if (unsolved.isJsonArray()) {
            return "simultaneous";
        }

        if (unsolved.isJsonPrimitive() && unsolved.getAsJsonPrimitive().isString()) {
            return "basic";
        }

        return "";
    }

    private static <T> T checked(T t, Supplier<String> message) throws JsonParseException {
        if (t == null) {
            throw new JsonParseException(message.get());
        }
        return t;
    }

    void write(AcousticsFile context, JsonObjectWriter writer) throws IOException;

    /**
     * Plays a sound.
     */
    void playSound(SoundPlayer player, LivingEntity location, State event, Options inputOptions);

    public interface Serializer {
        Acoustic create(JsonElement json, AcousticsFile context);

        static Serializer ofJsObject(BiFunction<JsonObject, AcousticsFile, Acoustic> factory) {
            return (json, context) -> factory.apply(json.getAsJsonObject(), context);
        }
    }
}