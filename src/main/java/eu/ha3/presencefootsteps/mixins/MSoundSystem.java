package eu.ha3.presencefootsteps.mixins;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import eu.ha3.presencefootsteps.sound.player.ImmediateSoundPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

@Mixin(SoundEngine.class)
abstract class MSoundSystem {
    @Shadow
    protected abstract float getVolume(@Nullable SoundSource category);

    @Inject(method = "calculateVolume(Lnet/minecraft/client/resources/sounds/SoundInstance;)F", at = @At("HEAD"), cancellable = true)
    private void onGetAdjustedVolume(SoundInstance sound, CallbackInfoReturnable<Float> info) {
        if (sound instanceof ImmediateSoundPlayer.UncappedSoundInstance t) {
            info.setReturnValue(Mth.clamp(t.getVolume() * getVolume(t.getSource()), 0, t.getMaxVolume()));
        }
    }
}
