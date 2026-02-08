package eu.ha3.presencefootsteps.config;

import java.lang.reflect.Type;
import net.minecraft.util.Mth;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.minelittlepony.common.client.gui.IField.IChangeCallback;

public class VolumeOption implements IChangeCallback<Float> {

    private transient final JsonFile config;

    private int value;

    public VolumeOption(JsonFile config, int value) {
        this.config = config;
        this.value = value;
    }

    public int get() {
        return Mth.clamp(value, 0, 100);
    }

    public float getPercentage() {
        return get() / 100F;
    }

    public float set(float volume) {
        value = volumeScaleToInt(volume);
        config.save();
        return get();
    }

    @Override
    public Float perform(Float value) {
        return set(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    private static int volumeScaleToInt(float volume) {
        return volume > 97 ? 100 : volume < 3 ? 0 : (int)volume;
    }

    static class Adapter implements JsonSerializer<VolumeOption>, JsonDeserializer<VolumeOption> {
        private final JsonFile file;
        Adapter(JsonFile file) {
            this.file = file;
        }
        @Override
        public JsonElement serialize(VolumeOption src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.get());
        }

        @Override
        public VolumeOption deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            return new VolumeOption(file, context.deserialize(json, int.class));
        }
    }

}
