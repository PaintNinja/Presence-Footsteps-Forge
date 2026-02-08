package eu.ha3.presencefootsteps.sound.acoustics;

import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.sound.player.SoundPlayer;
import eu.ha3.presencefootsteps.util.JsonObjectWriter;
import java.io.IOException;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;

/**
 * An acoustic that plays multiple other acoustics all at the same time.
 *
 * @author Hurry
 */
record SimultaneousAcoustic(List<Acoustic> acoustics) implements Acoustic {
    static final Serializer FACTORY = (json, context) -> new SimultaneousAcoustic(
            (json.isJsonArray() ? json.getAsJsonArray() : json.getAsJsonObject().getAsJsonArray("array")).asList()
            .stream()
            .map(i -> Acoustic.read(context, i))
            .toList()
    );

    @Override
    public void playSound(SoundPlayer player, LivingEntity location, State event, Options inputOptions) {
        acoustics.forEach(acoustic -> acoustic.playSound(player, location, event, inputOptions));
    }

    @Override
    public void write(AcousticsFile context, JsonObjectWriter writer) throws IOException {
        writer.array(() -> writer.each(acoustics, c -> c.write(context, writer)));
    }
}