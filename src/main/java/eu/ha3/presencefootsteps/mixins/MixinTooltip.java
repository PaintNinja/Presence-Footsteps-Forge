package eu.ha3.presencefootsteps.mixins;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Tooltip.class)
public interface MixinTooltip {
    @Accessor("cachedTooltip")
    void setLines(List<FormattedCharSequence> lines);
}
