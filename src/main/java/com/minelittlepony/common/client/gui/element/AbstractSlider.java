package com.minelittlepony.common.client.gui.element;

import com.minelittlepony.common.client.gui.IField;
import com.minelittlepony.common.client.gui.Tooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.function.Function;

/**
 * Base class for a slider element.
 *
 * @author     Sollace
 *
 * @param <T> The value type for this slider.
 */
public abstract class AbstractSlider<T> extends Button implements IField<T, AbstractSlider<T>> {

    private float min;
    private float max;

    private float value;

    @NotNull
    private IChangeCallback<T> action = IChangeCallback::none;

    @Nullable
    private Function<AbstractSlider<T>, Component> textFunc;
    @Nullable
    private Function<AbstractSlider<T>, Tooltip> tooltipFunc;

    public AbstractSlider(int x, int y, float min, float max, T value) {
        super(x, y);

        this.min = min;
        this.max = max;
        this.value = convertFromRange(valueToFloat(value), min, max);
    }

    protected abstract float valueToFloat(T value);

    protected abstract T floatToValue(float value);

    @Override
    public AbstractSlider<T> onChange(@NotNull IChangeCallback<T> action) {
        this.action = action;
        return this;
    }

    /**
     * Sets a function to use when formatting the slider's current value for display.
     *
     * @param formatter The formatting function to call.
     * @return {@code this} for chaining purposes
     */
    public AbstractSlider<T> setTextFormat(@NotNull Function<AbstractSlider<T>, Component> formatter) {
        this.textFunc = formatter;
        getStyle().setText(formatter.apply(this));

        return this;
    }
    /**
     * Sets a function to use when formatting the slider's current value for display in its tooltip.
     *
     * @param formatter The formatting function to call.
     * @return {@code this} for chaining purposes
     */
    public AbstractSlider<T> setTooltipFormat(@NotNull Function<AbstractSlider<T>, Tooltip> formatter) {
        this.tooltipFunc = formatter;
        getStyle().setTooltip(formatter.apply(this));

        return this;
    }

    @Override
    public AbstractSlider<T> setValue(T value) {
        setClampedValue(convertFromRange(valueToFloat(value), min, max));

        return this;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (active && visible && (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT)) {
            playDownSound(Minecraft.getInstance().getSoundManager());

            float step = (max - min) / 4F;

            if (keyCode == GLFW.GLFW_KEY_LEFT) {
                step *= -1;
            }

            setClampedValue(value + step);
            onPress();

            return true;
        }
        return false;
    }

    protected void setClampedValue(float value) {
        value = Mth.clamp(value, 0, 1);

        if (value != this.value) {
            float initial = this.value;
            this.value = value;
            this.value = convertFromRange(valueToFloat(action.perform(getValue())), min, max);

            if (this.value != initial) {
                if (textFunc != null) {
                    getStyle().setText(textFunc.apply(this));
                }
                if (tooltipFunc != null) {
                    getStyle().setTooltip(tooltipFunc.apply(this));
                }
            }
        }
    }

    private void onChange(double mouseX) {
        // convert pixel coordinate to range (0 - 1)
        setClampedValue((float)(mouseX - (getX() + 4)) / (getWidth() - 8));
    }

    @Override
    public T getValue() {
        return floatToValue(convertToRange(value, min, max));
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        onChange(mouseX);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double mouseDX, double mouseDY) {
        onChange(mouseX);
    }

    @Override
    protected void renderBackground(GuiGraphics context, Minecraft mc, int mouseX, int mouseY) {
        context.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURES.get(false, isHoveredOrFocused()), getX(), getY(), getWidth(), getHeight(), ARGB.white(alpha));
        int sliderX = getX() + (int)(value * (getWidth() - 8));
        context.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURES.get(active, isHoveredOrFocused()), sliderX, getY(), 8, getHeight(), ARGB.white(alpha));
    }

    static float convertFromRange(float value, float min, float max) {
        return (Mth.clamp(value, min, max) - min) / (max - min);
    }

    static float convertToRange(float value, float min, float max) {
        return Mth.clamp(min + (value * (max - min)), min, max);
    }
}
