package com.minelittlepony.common.client.gui;

import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.common.client.gui.dimension.Padding;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

import java.util.List;

interface IViewRootDefaultImpl extends IViewRoot {
    @Override
    default Bounds getBounds() { throw new RuntimeException("stub"); }
    @Override
    default void setBounds(Bounds bounds) { throw new RuntimeException("stub"); }
    @Override
    default Padding getContentPadding() { throw new RuntimeException("stub"); }
    @Override
    default <T extends GuiEventListener & Renderable & NarratableEntry> List<NarratableEntry> buttons() { throw new RuntimeException("stub"); }
    @Override
    default <T extends GuiEventListener & Renderable & NarratableEntry> T addButton(T button) { throw new RuntimeException("stub"); }
    @Override
    default List<GuiEventListener> getChildElements() { throw new RuntimeException("stub"); }
}
