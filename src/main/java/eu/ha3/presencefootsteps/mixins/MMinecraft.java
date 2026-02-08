package eu.ha3.presencefootsteps.mixins;

import com.minelittlepony.common.client.gui.ITickableElement;
import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
abstract class MMinecraft {
    @Inject(method = "resizeDisplay()V", at = @At(
            value = "INVOKE",
            target = "net/minecraft/client/gui/screens/Screen.resize(Lnet/minecraft/client/Minecraft;II)V",
            shift = Shift.AFTER
        )
    )
    private void onOnResolutionChanged(CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.screen instanceof IViewRoot root) {
            Bounds bounds = root.getBounds();
            bounds.width = client.getWindow().getGuiScaledWidth();
            bounds.height = client.getWindow().getGuiScaledHeight();
        }
    }

    @Inject(method = "tick()V", at = @At("HEAD"))
    public void onTick(CallbackInfo info) {
        Minecraft client = Minecraft.getInstance();
        if (client.screen instanceof IViewRoot root) {
            root.getChildElements().forEach(element -> {
                if (element instanceof ITickableElement t) {
                    t.tick();
                }
            });
        }
    }
}
