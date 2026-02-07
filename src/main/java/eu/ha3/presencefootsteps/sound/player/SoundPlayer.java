package eu.ha3.presencefootsteps.sound.player;

import java.util.Random;
import net.minecraft.world.entity.LivingEntity;
import eu.ha3.presencefootsteps.sound.Options;

public interface SoundPlayer {
    /**
     * Plays a sound.
     */
    void playSound(LivingEntity location, String soundName, float volume, float pitch, Options options);

    /**
     * Returns a random number generator.
     */
    Random getRNG();

    default void think() { }
}
