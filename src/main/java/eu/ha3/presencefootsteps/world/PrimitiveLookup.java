package eu.ha3.presencefootsteps.world;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;
import com.google.gson.JsonObject;

import eu.ha3.presencefootsteps.util.JsonObjectWriter;

public class PrimitiveLookup extends AbstractSubstrateLookup<SoundEvent> {
    public PrimitiveLookup(JsonObject json) {
        super(json);
    }

    @Override
    protected ResourceLocation getId(SoundEvent key) {
        return key.getLocation();
    }

    public static void writeToReport(Lookup<SoundEvent> lookup, boolean full, JsonObjectWriter writer, Map<String, SoundType> groups) throws IOException {
        writer.each(groups.values(), group -> {
            SoundEvent event = group.getStepSound();
            if (event != null && (full || !lookup.contains(event))) {
                writer.field(getKey(group), lookup.getAssociation(event, getSubstrate(group)).raw());
            }
        });
    }

    public static String getSubstrate(SoundType group) {
        return String.format(Locale.ENGLISH, "%.2f_%.2f", group.volume, group.pitch);
    }

    public static String getKey(SoundType group) {
        return group.getStepSound().getLocation().toString() + "@" + getSubstrate(group);
    }
}
