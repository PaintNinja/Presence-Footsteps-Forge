package eu.ha3.presencefootsteps.sound.player;

import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import eu.ha3.presencefootsteps.util.PlayerUtil;
import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.sound.SoundEngine;

/**
 * A Library that can also play sounds and default footsteps.
 *
 * @author Hurry
 */
public final class ImmediateSoundPlayer implements SoundPlayer {
    private final Random random = new Random();
    private final SoundEngine engine;

    public ImmediateSoundPlayer(SoundEngine engine) {
        this.engine = engine;
    }

    @Override
    public Random getRNG() {
        return random;
    }

    @Override
    public void playSound(LivingEntity location, String soundName, float volume, float pitch, Options options) {
        volume *= options.getOrDefault("volume_percentage", 1F);
        pitch *= options.getOrDefault("pitch_percentage", 1F);

        Minecraft mc = Minecraft.getInstance();
        double distance = mc.gameRenderer.getMainCamera().getPosition().distanceToSqr(location.position());

        volume *= engine.getVolumeForSource(location);
        pitch /= ((PlayerUtil.getScale(location) - 1) * 0.6F) + 1;

        SimpleSoundInstance sound = new UncappedSoundInstance(soundName, volume, pitch, location);

        if (distance > 100) {
            mc.getSoundManager().playDelayed(sound, (int) Math.floor(Math.sqrt(distance) / 2));
        } else {
            mc.getSoundManager().play(sound);
        }
    }

    public static class UncappedSoundInstance extends SimpleSoundInstance {
        public UncappedSoundInstance(String soundName, float volume, float pitch, Entity entity) {
            super(getSoundId(soundName, entity),
                    entity.getSoundSource(),
                    volume, pitch, SoundInstance.createUnseededRandom(), false, 0,
                    SoundInstance.Attenuation.LINEAR,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    false);
        }

        public float getMaxVolume() {
            return 3;
        }

        private static ResourceLocation getSoundId(String name, Entity location) {
            if (name.indexOf(':') >= 0) {
                return ResourceLocation.parse(name);
            }

            String domain = "presencefootsteps";

            if (!PlayerUtil.isClientPlayer(location)) {
                domain += "mono"; // Switch to mono if playing another player
            }

            return ResourceLocation.fromNamespaceAndPath(domain, name);
        }
    }
}