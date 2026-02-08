package eu.ha3.presencefootsteps.mixins;

import com.minelittlepony.common.client.gui.ITextContext;
import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.common.client.gui.dimension.Padding;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Screen.class)
abstract class MScreen extends AbstractContainerEventHandler implements Renderable, IViewRoot, ITextContext {

    private final Bounds bounds = new Bounds(0, 0, 0, 0);

    private final Padding padding = new Padding(0, 0, 0, 0);

    @Shadow
    public @Final List<Renderable> renderables;

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public void setBounds(Bounds bounds) { }

    @SuppressWarnings("unchecked")
    @Override
    public List<GuiEventListener> getChildElements() {
        return (List<GuiEventListener>) ((Screen) (Object)this).children();
    }

    @Override
    public Padding getContentPadding() {
        return padding;
    }

    @Override
    @Accessor("narratables")
    public abstract List<NarratableEntry> buttons();

    @Override
    public <T extends GuiEventListener & Renderable & NarratableEntry> T addButton(T button) {
        buttons().add(button);
        renderables.add(button);
        getChildElements().add(button);
        return button;
    }

    @Inject(method = "init(Lnet/minecraft/client/Minecraft;II)V", at = @At("RETURN"))
    private void onInit(Minecraft client, int w, int h, CallbackInfo ci) {
        bounds.width = w;
        bounds.height = h;
    }
}
