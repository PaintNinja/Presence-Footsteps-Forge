package eu.ha3.presencefootsteps.sound.acoustics;

import java.io.IOException;
import net.minecraft.world.entity.LivingEntity;
import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.sound.player.SoundPlayer;
import eu.ha3.presencefootsteps.util.JsonObjectWriter;
import eu.ha3.presencefootsteps.util.Period;

record DelayedAcoustic(
        Acoustic acoustic,
        Period delay
) implements Acoustic {
    static final Serializer FACTORY = Serializer.ofJsObject((json, context) -> new DelayedAcoustic(
        json.has("name") ? VaryingAcoustic.FACTORY.create(json, context) : Acoustic.read(context, json.get("acoustic")),
        Period.fromJson(json, "delay")
    ));

    @Override
    public void playSound(SoundPlayer player, LivingEntity location, State event, Options inputOptions) {
        acoustic.playSound(player, location, event, inputOptions.and(delay));
    }

    @Override
    public void write(AcousticsFile context, JsonObjectWriter writer) throws IOException {
        writer.object(() -> {
            writer.field("type", "delayed");
            writer.field("delay", () -> delay.write(writer));
            writer.field("acoustic", () -> acoustic.write(context, writer));
        });
    }
}