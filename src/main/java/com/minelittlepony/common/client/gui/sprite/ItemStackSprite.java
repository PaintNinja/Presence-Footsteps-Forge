package com.minelittlepony.common.client.gui.sprite;

import com.minelittlepony.common.client.gui.OutsideWorldRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.ItemLike;

public class ItemStackSprite implements ISprite {

    private ItemStack stack = ItemStack.EMPTY;

    private int tint = CommonColors.WHITE;

    private boolean renderFailed;
    private boolean needsWorld;

    public ItemStackSprite setStack(ItemLike iitem) {
        return setStack(new ItemStack(iitem));
    }

    public ItemStackSprite setStack(ItemStack stack) {
        this.stack = stack;
        renderFailed = false;
        needsWorld = false;

        return setTint(tint);
    }

    public ItemStackSprite setTint(int tint) {
        this.tint = tint;
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(tint));
        return this;
    }

    @Override
    public void render(GuiGraphics context, int x, int y, int mouseX, int mouseY, float partialTicks) {
        if (renderFailed) {
            return;
        }

        if (!needsWorld) {
            try {
                context.renderItem(stack, x + 2, y + 2);
                return;
            } catch (Throwable ignored) {
                needsWorld = true;
            }
        }

        try {
            OutsideWorldRenderer.configure(null);
            context.renderItem(stack, x + 2, y + 2);
        } catch (Throwable ignored) {
            renderFailed = true;
        }
    }
}
