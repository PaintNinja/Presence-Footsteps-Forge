package eu.ha3.presencefootsteps.sound.acoustics;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.sound.player.SoundPlayer;
import eu.ha3.presencefootsteps.util.JsonObjectWriter;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;

/**
 *
 * An acoustic that can pick from more than one sound to play, each with their own relative
 * weighting for how often that sound is picked.
 *
 * @author Hurry
 *
 */
record WeightedAcoustic(
        Entry[] entries
) implements Acoustic {
    static final Serializer FACTORY = Serializer.ofJsObject((json, context) -> {
        List<Entry> entries = new ObjectArrayList<>();
        Iterator<JsonElement> iter = json.getAsJsonArray(json.has("array") ? "array" : "entries").iterator();
        while (iter.hasNext()) {
            int weight = iter.next().getAsInt();

            if (!iter.hasNext()) {
                throw new JsonParseException("Probability has odd number of children!");
            }

            entries.add(new Entry(weight, Acoustic.read(context, iter.next())));
        }

        return new WeightedAcoustic(entries.toArray(Entry[]::new));
    });

    WeightedAcoustic {
        float total = 0;
        for (Entry entry : entries) {
            Preconditions.checkArgument(entry.weight >= 0, "A probability weight can't be negative");
            total += entry.weight;
        }
        if (total < 0) {
            Preconditions.checkArgument(total >= 0, "A probability weight can't be negative");
        }

        for (Entry entry : entries) {
            entry.threshold = entry.weight / total;
        }
    }

    @Override
    public void playSound(SoundPlayer player, LivingEntity location, State event, Options inputOptions) {
        final float rand = player.getRNG().nextFloat();
        int marker = -1;
        while (++marker < entries.length) {
            if (entries[marker].threshold >= rand) {
                entries[marker].acoustic.playSound(player, location, event, inputOptions);
                return;
            }
        }
    }

    @Override
    public void write(AcousticsFile context, JsonObjectWriter writer) throws IOException {
        writer.object(() -> {
            writer.field("type", "probability");
            writer.array("entries", () -> {
                for (Entry entry : entries) {
                    writer.writer().value(entry.weight);
                    entry.acoustic.write(context, writer);
                }
            });
        });
    }

    private static class Entry {
        private final Acoustic acoustic;
        private final int weight;
        private float threshold;

        Entry(int weight, Acoustic acoustic) {
            this.weight = weight;
            this.acoustic = acoustic;
        }
    }
}