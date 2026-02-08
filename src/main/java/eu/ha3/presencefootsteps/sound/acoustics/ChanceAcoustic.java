package eu.ha3.presencefootsteps.sound.acoustics;

import java.io.IOException;
import net.minecraft.world.entity.LivingEntity;
import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.sound.player.SoundPlayer;
import eu.ha3.presencefootsteps.util.JsonObjectWriter;

record ChanceAcoustic(
        Acoustic acoustic,
        float probability
) implements Acoustic {
    static final Serializer FACTORY = Serializer.ofJsObject((json, context) -> new ChanceAcoustic(
        Acoustic.read(context, json.get("acoustic")),
        json.get("probability").getAsFloat()
    ));

    @Override
    public void playSound(SoundPlayer player, LivingEntity location, State event, Options inputOptions) {
        if (player.getRNG().nextFloat() * 100 <= probability) {
            acoustic.playSound(player, location, event, inputOptions);
        }
    }

    @Override
    public void write(AcousticsFile context, JsonObjectWriter writer) throws IOException {
        writer.object(() -> {
            writer.field("type", "chance");
            writer.field("probability", probability);
            writer.field("acoustic", () -> acoustic.write(context, writer));
        });
    }
}