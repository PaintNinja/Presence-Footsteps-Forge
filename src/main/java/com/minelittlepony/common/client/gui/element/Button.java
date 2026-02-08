package com.minelittlepony.common.client.gui.element;

import com.minelittlepony.common.client.gui.ITextContext;
import com.minelittlepony.common.client.gui.ITickableElement;
import com.minelittlepony.common.client.gui.Tooltip;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.common.client.gui.dimension.IBounded;
import com.minelittlepony.common.client.gui.style.IStyled;
import com.minelittlepony.common.client.gui.style.Style;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.WidgetTooltipHolder;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A stylable button element.
 * <p>
 * All appearance other than dimensions and position are controlled by this element's {Style}
 * to make switching and changing styles easier.
 *
 * @author     Sollace
 *
 */
public class Button extends AbstractButton implements IBounded, ITextContext, IStyled<Button>, ITickableElement {
    protected static final WidgetSprites TEXTURES = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("widget/button"),
            ResourceLocation.withDefaultNamespace("widget/button_disabled"),
            ResourceLocation.withDefaultNamespace("widget/button_highlighted")
    );

    private Style style = new Style();

    private final Bounds bounds;

    private static final Consumer<Button> NONE = v -> {};
    @NotNull
    private Consumer<Button> action = NONE;
    @NotNull
    private Consumer<Button> update = NONE;
    @Nullable
    private Tooltip prevTooltip;

    public Button(int x, int y) {
        this(x, y, 200, 20);
    }

    public Button(int x, int y, int width, int height) {
        super(x, y, width, height, CommonComponents.EMPTY);
        tooltip = new WidgetTooltipHolder() {
            @Override
            public void refreshTooltipForNextRenderPass(GuiGraphics context, int mouseX, int mouseY, boolean hovered, boolean focused, ScreenRectangle navigationFocus) {
                getStyle().getTooltip().ifPresentOrElse(tooltip -> {
                    if (tooltip != prevTooltip) {
                        prevTooltip = tooltip;
                        set(tooltip.toTooltip(Button.this));
                    }
                }, () -> set(null));
                super.refreshTooltipForNextRenderPass(context, mouseX, mouseY, hovered, focused, navigationFocus);
            }

            @Override
            public ClientTooltipPositioner createTooltipPositioner(ScreenRectangle focus, boolean hovered, boolean focused) {
                final ClientTooltipPositioner positioner = super.createTooltipPositioner(focus, hovered, focused);
                return (sw, sh, x, y, w, h) -> positioner.positionTooltip(sw, sh, x, y, w, h).add(getStyle().toolTipX, getStyle().toolTipY, new Vector2i());
            }
        };
        bounds = new Bounds(y, x, width, height);
    }

    /**
     * Adds a listener to call when this button is clicked.
     *
     * @param callback The callback function.
     * @return {@code this} for chaining purposes.
     */
    @SuppressWarnings("unchecked")
    public Button onClick(@NotNull Consumer<? extends Button> callback) {
        action = (Consumer<Button>)Objects.requireNonNull(callback);

        return this;
    }

    /**
     * Adds a listener to call every tick for this element.
     *
     * @param callback The callback function.
     * @return {@code this} for chaining purposes.
     */
    @SuppressWarnings("unchecked")
    public Button onUpdate(@NotNull Consumer<? extends Button> callback) {
        update = (Consumer<Button>)Objects.requireNonNull(callback);

        return this;
    }

    /**
     * Enables or disables this button.
     */
    public Button setEnabled(boolean enable) {
        active = enable;
        return this;
    }

    /**
     * Hides or shows this button.
     */
    public Button setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    /**
     * Gets this button's current styling.
     */
    @Override
    public Style getStyle() {
        return style;
    }

    /**
     * Sets this button's current styling.
     */
    @Override
    public Button setStyle(Style style) {
        this.style = style;

        return this;
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public void setBounds(Bounds bounds) {
        this.bounds.copy(bounds);

        setX(bounds.left);
        setY(bounds.top);
        setWidth(bounds.width);
        setHeight(bounds.height);
    }

    @Override
    public void setX(int x) {
        bounds.left = x;
        super.setX(x);
    }

    @Override
    public void setY(int y) {
        bounds.top = y;
        super.setY(y);
    }

    @Override
    public void setWidth(int width) {
        bounds.width = width;
        super.setWidth(width);
    }

    @Override
    public void setHeight(int height) {
        bounds.height = height;
        this.height = height;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationMsg) {
        getStyle().getTooltip().ifPresent(tooltip -> tooltip.updateNarration(narrationMsg));
    }

    @Override
    public void onPress(InputWithModifiers input) {
        action.accept(this);
    }

    @Override
    public void tick() {
        update.accept(this);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return active && visible && getBounds().contains(mouseX, mouseY);
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float tickDelta) {
        this.isHovered = isMouseOver(mouseX, mouseY);
        Minecraft mc = Minecraft.getInstance();
        renderBackground(context, mc, mouseX, mouseY);
        setMessage(getStyle().getText());
        drawIcon(context, mouseX, mouseY, tickDelta);

        int foreColor = getStyle().getColor();
        if (!active) {
            foreColor = 10526880;
        } else if (isHovered()) {
            foreColor = 16777120;
        }
        renderForeground(context, mc, mouseX, mouseY, foreColor | Mth.ceil(alpha * 255F) << 24);
        if (getBounds().contains(mouseX, mouseY)) {
            context.requestCursor(isActive() ? getCursor(mouseX, mouseY) : CursorTypes.NOT_ALLOWED);
        }
    }

    protected CursorType getCursor(int mouseX, int mouseY) {
        return CursorTypes.POINTING_HAND;
    }

    protected void renderBackground(GuiGraphics context, Minecraft mc, int mouseX, int mouseY) {
        context.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                TEXTURES.get(active, isHoveredOrFocused()),
                getX(), getY(),
                getWidth(), getHeight(),
                ARGB.white(alpha)
        );
    }

    protected void drawIcon(GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        if (getStyle().hasIcon()) {
            getStyle().getIcon().render(context, getX(), getY(), mouseX, mouseY, partialTicks, alpha);
        }
    }

    protected void renderForeground(GuiGraphics context, Minecraft mc, int mouseX, int mouseY, int foreColor) {
        renderString(context, mc.font, foreColor);
    }

    @Override
    public void renderString(GuiGraphics context, Font textRenderer, int color) {
        Bounds bounds = getBounds();
        int left = getStyle().getIcon().getBounds().right();
        renderScrollingString(context, textRenderer, getMessage(), bounds.left + left, bounds.top, bounds.right() - 2, bounds.bottom(), color);
    }
}
