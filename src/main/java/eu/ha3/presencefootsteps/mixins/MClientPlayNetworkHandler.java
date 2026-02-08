package eu.ha3.presencefootsteps.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import eu.ha3.presencefootsteps.PresenceFootsteps;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;

@Mixin(ClientPacketListener.class)
public abstract class MClientPlayNetworkHandler implements ClientGamePacketListener {

    @Inject(method = "onPlaySound(Lnet/minecraft/network/packet/s2c/play/PlaySoundS2CPacket;)V",
            at = @At(value = "INVOKE", target = "net/minecraft/client/world/ClientWorld.playSound("
                        + "Lnet/minecraft/entity/player/PlayerEntity;"
                        + "DDD"
                        + "Lnet/minecraft/registry/entry/RegistryEntry;"
                        + "Lnet/minecraft/sound/SoundCategory;"
                        + "FFJ"
                    + ")V",
                    shift = Shift.BEFORE
            ),
            cancellable = true
    )
    public void onHandleSoundEffect(ClientboundSoundPacket packet, CallbackInfo info) {
        if (PresenceFootsteps.getInstance().getEngine().onSoundRecieved(packet)) {
            info.cancel();
        }
    }
}
