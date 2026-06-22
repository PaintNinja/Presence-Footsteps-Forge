package eu.ha3.presencefootsteps.sound;

import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public final class SoundSourceUtil {
    private static final Set<Identifier> PLAYER_STEP_SOUNDS = Set.of(
            SoundEvents.ENTITY_PLAYER_SWIM.getId(),
            SoundEvents.ENTITY_PLAYER_SPLASH.getId(),
            SoundEvents.ENTITY_PLAYER_BIG_FALL.getId(),
            SoundEvents.ENTITY_PLAYER_SMALL_FALL.getId(),
            SoundEvents.ENTITY_PLAYER_SPLASH_HIGH_SPEED.getId()
    );

    public static boolean isHorseGallopingSound(Identifier id) {
        return id.getPath().contains("horse.gallop");
    }

    public static boolean isStepSound(Identifier id) {
        return id.getPath().endsWith(".step");
    }

    public static boolean isPlayerStepSound(SoundCategory source, Identifier id, @Nullable SoundEvent steppedAtPosSound) {
        return PLAYER_STEP_SOUNDS.contains(id)
                || (steppedAtPosSound != null && source == SoundCategory.PLAYERS && id.equals(steppedAtPosSound.getId()));
    }

    public static @Nullable SoundEvent getStepSoundAtPosition(BlockPos pos) {
        @Nullable ClientWorld world = MinecraftClient.getInstance().world;
        return world == null ? null : world.getBlockState(pos).getSoundGroup().getStepSound();
    }
}
