package eu.ha3.presencefootsteps.sound;

import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public final class SoundSourceUtil {
    private static final Set<Identifier> PLAYER_STEP_SOUNDS = Set.of(
            SoundEvents.PLAYER_SWIM.location(),
            SoundEvents.PLAYER_SPLASH.location(),
            SoundEvents.PLAYER_BIG_FALL.location(),
            SoundEvents.PLAYER_SMALL_FALL.location(),
            SoundEvents.PLAYER_SPLASH_HIGH_SPEED.location()
    );

    public static boolean isHorseGallopingSound(Identifier id) {
        return id.getPath().contains("horse.gallop");
    }

    public static boolean isStepSound(Identifier id) {
        return id.getPath().endsWith(".step");
    }

    public static boolean isPlayerStepSound(SoundSource source, Identifier id, @Nullable SoundEvent steppedAtPosSound) {
        return PLAYER_STEP_SOUNDS.contains(id)
                || (steppedAtPosSound != null && source == SoundSource.PLAYERS && id.equals(steppedAtPosSound.location()));
    }

    public static @Nullable SoundEvent getStepSoundAtPosition(BlockPos pos) {
        @Nullable ClientLevel world = Minecraft.getInstance().level;
        return world == null ? null : world.getBlockState(pos).getSoundType().getStepSound();
    }
}
