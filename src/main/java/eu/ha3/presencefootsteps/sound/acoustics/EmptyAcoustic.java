package eu.ha3.presencefootsteps.sound.acoustics;

import java.io.IOException;
import net.minecraft.world.entity.LivingEntity;
import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.sound.player.SoundPlayer;
import eu.ha3.presencefootsteps.util.JsonObjectWriter;

record EmptyAcoustic() implements Acoustic {
    static Acoustic INSTANCE = new EmptyAcoustic();

    @Override
    public void playSound(SoundPlayer player, LivingEntity location, State event, Options inputOptions) {
    }

    @Override
    public void write(AcousticsFile context, JsonObjectWriter writer) throws IOException {
        writer.object(() -> {});
    }
}