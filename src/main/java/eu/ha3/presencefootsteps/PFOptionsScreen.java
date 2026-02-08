package eu.ha3.presencefootsteps;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.minelittlepony.common.client.gui.IViewRootDefaultImpl;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.common.client.gui.ScrollContainer;
import com.minelittlepony.common.client.gui.Tooltip;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.common.client.gui.element.AbstractSlider;
import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.client.gui.element.EnumSlider;
import com.minelittlepony.common.client.gui.element.Label;
import com.minelittlepony.common.client.gui.element.Slider;
import com.minelittlepony.common.client.gui.element.Toggle;

import eu.ha3.presencefootsteps.config.VolumeOption;
import eu.ha3.presencefootsteps.sound.Isolator;
import eu.ha3.presencefootsteps.sound.acoustics.Acoustic;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticsFile;
import eu.ha3.presencefootsteps.util.BlockReport;
import eu.ha3.presencefootsteps.util.ResourceUtils;

class PFOptionsScreen extends GameGui {
    public static final Component TITLE = Component.translatable("menu.pf.title");
    public static final Component UP_TO_DATE = Component.translatable("pf.update.up_to_date");
    public static final Component VOLUME_MIN = Component.translatable("menu.pf.volume.min");

    private final ScrollContainer content = new ScrollContainer();

    public PFOptionsScreen(@Nullable Screen parent) {
        super(Component.translatable("%s (%s)", TITLE, PresenceFootsteps.getInstance().getOptionsKeyBinding().getTranslatedKeyMessage()), parent);
        content.margin.top = 30;
        content.margin.bottom = 30;
        content.getContentPadding().top = 10;
        content.getContentPadding().right = 10;
        content.getContentPadding().bottom = 20;
        content.getContentPadding().left = 10;
    }

    @Override
    protected void init() {
        content.init(this::rebuildContent);
    }

    private void rebuildContent() {
        int left = content.width / 2 - 100;

        int wideLeft = content.width / 2 - 165;
        int wideRight = wideLeft + 160;

        int row = 0;

        PFConfig config = PresenceFootsteps.getInstance().getConfig();

        getChildElements().add(content);

        addButton(new Label(width / 2, 10)).setCentered().getStyle().setText(getTitle());

        Toggle disabledToggle = new Toggle(wideLeft, row, config.getDisabled());
        content.addButton(disabledToggle.onChange(disabled -> {
            updateDisableState(disabledToggle, config.setDisabled(disabled));
            return disabled;
        })).getStyle().setText("menu.pf.disable_mod");

        content.addButton(new Label(wideLeft, row += 24)).getStyle().setText("menu.pf.group.volume");

        var slider = content.addButton(new Slider(wideLeft, row += 24, 0, 100, config.getGlobalVolume()))
            .onChange(config::setGlobalVolume)
            .setTextFormat(this::formatVolume);
        slider.setBounds(new Bounds(row, wideLeft, 310, 20));
        slider.getStyle().setTooltip(Tooltip.of("menu.pf.volume.tooltip", 210)).setTooltipOffset(0, 25);

        row += 10;

        addVolumeSlider(wideLeft, row += 24, config.clientPlayerVolume, "player");
        addVolumeSlider(wideRight, row, config.otherPlayerVolume, "other_players");

        addVolumeSlider(wideLeft, row += 24, config.hostileEntitiesVolume, "hostile_entities");
        addVolumeSlider(wideRight, row, config.passiveEntitiesVolume, "passive_entities");

        addVolumeSlider(wideLeft, row += 24, config.wetSoundsVolume, "wet");
        addVolumeSlider(wideRight, row, config.foliageSoundsVolume, "foliage");
        row += 10;

        slider = content.addButton(new Slider(wideLeft, row += 24, -100, 100, config.getRunningVolumeIncrease()))
            .onChange(config::setRunningVolumeIncrease)
            .setTextFormat(formatVolume("menu.pf.volume.running"));
        slider.setBounds(new Bounds(row, wideLeft, 310, 20));
        slider.getStyle().setTooltip(Tooltip.of("menu.pf.volume.running.tooltip", 210)).setTooltipOffset(0, 25);

        content.addButton(new Label(wideLeft, row += 25)).getStyle().setText("menu.pf.group.footsteps");

        content.addButton(new EnumSlider<>(left, row += 24, config.getLocomotion())
                .onChange(config::setLocomotion)
                .setTextFormat(v -> v.getValue().getOptionName()))
                .setTooltipFormat(v -> Tooltip.of(v.getValue().getOptionTooltip(), 250))
                .setBounds(new Bounds(row, wideLeft, 310, 20));

        content.addButton(new Button(wideLeft, row += 24, 150, 20).onClick(sender -> {
            sender.getStyle().setText("menu.pf.global." + config.cycleTargetSelector().name().toLowerCase());
        })).getStyle()
            .setText("menu.pf.global." + config.getEntitySelector().name().toLowerCase());

        content.addButton(new Button(wideRight, row, 150, 20).onClick(sender -> {
            sender.getStyle().setText("menu.pf.multiplayer." + config.toggleMultiplayer());
        })).getStyle()
            .setText("menu.pf.multiplayer." + config.getEnabledMP());

        content.addButton(new Button(wideLeft, row += 24, 150, 20).onClick(sender -> {
            sender.getStyle().setText("menu.pf.footwear." + (config.toggleFootwear() ? "on" : "off"));
        })).getStyle()
            .setText("menu.pf.footwear." + (config.getEnabledFootwear() ? "on" : "off"));

        content.addButton(new Button(wideRight, row, 150, 20).onClick(sender -> {
            sender.getStyle().setText("menu.pf.exclusive_mode." + (config.toggleExclusiveMode() ? "on" : "off"));
        })).getStyle()
            .setText("menu.pf.exclusive_mode." + (config.isExclusiveMode() ? "on" : "off"));

        content.addButton(new Label(wideLeft, row += 25)).getStyle().setText("menu.pf.group.sound_packs");

        content.addButton(new Button(wideLeft, row += 25, 150, 20).onClick(sender -> {
            minecraft.setScreen(new PackSelectionScreen(
                    minecraft.getResourcePackRepository(),
                    manager -> {
                        minecraft.options.updateResourcePacks(manager);
                        minecraft.setScreen(this);
                    },
                    minecraft.getResourcePackDirectory(),
                    Component.translatable("resourcePack.title")
            ));
        })).getStyle().setText("options.resourcepack");

        content.addButton(new Label(wideLeft, row += 25)).getStyle().setText("menu.pf.group.debugging");

        content.addButton(new Button(wideLeft, row += 25, 150, 20).onClick(sender -> {
            sender.setEnabled(false);
            BlockReport.execute(PresenceFootsteps.getInstance().getEngine().getIsolator(), "report_concise", false).thenRun(() -> sender.setEnabled(true));
        })).setEnabled(minecraft.level != null)
            .getStyle()
            .setText("menu.pf.report.concise");

        content.addButton(new Button(wideRight, row, 150, 20)
            .onClick(sender -> {
                sender.setEnabled(false);
                BlockReport.execute(PresenceFootsteps.getInstance().getEngine().getIsolator(), "report_full", true).thenRun(() -> sender.setEnabled(true));
            }))
            .setEnabled(minecraft.level != null)
            .getStyle()
                .setText("menu.pf.report.full");

        content.addButton(new Button(wideLeft, row += 25, 150, 20)
                .onClick(sender -> {
                    sender.setEnabled(false);
                    BlockReport.execute((full, writer, groups) -> {
                        ResourceUtils.forEach(Isolator.ACOUSTICS, minecraft.getResourceManager(), reader -> {
                            Map<String, Acoustic> acoustics = new HashMap<>();
                            AcousticsFile file = AcousticsFile.read(reader, acoustics::put, true);
                            if (file != null) {
                                try {
                                    file.write(writer, acoustics);
                                } catch (IOException e) {
                                    PresenceFootsteps.logger.error("Error whilst exporting acoustics", e);
                                }
                            }
                        });
                    }, "acoustics", true).thenRun(() -> sender.setEnabled(true));
                }))
                .setEnabled(minecraft.level != null)
                .getStyle()
                    .setText("menu.pf.report.acoustics");

        addButton(new Button(left, height - 25)
            .onClick(sender -> finish())).getStyle()
            .setText("gui.done");

        updateDisableState(disabledToggle, disabledToggle.getValue());
    }

    private void addVolumeSlider(int x, int y, VolumeOption option, String name) {
        var slider = content.addButton(new Slider(x, y, 0, 100, option.get()))
                .onChange(option)
                .setTextFormat(formatVolume("menu.pf.volume." + name));
        slider.setBounds(new Bounds(y, x, 150, 20));
        slider.styled(s -> s.setTooltip(Tooltip.of("menu.pf.volume." + name + ".tooltip", 210)).setTooltipOffset(0, 25));
    }

    private void updateDisableState(Toggle disabledToggle, boolean disabled) {
        content.children().forEach(child -> {
            if (child != disabledToggle && child instanceof Button button) {
                button.setEnabled(!disabled);
            }
        });
    }

    private Component formatVolume(AbstractSlider<Float> slider) {
        if (slider.getValue() <= 0) {
            return VOLUME_MIN;
        }

        return Component.translatable("menu.pf.volume", (int)Math.floor(slider.getValue()));
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float tickDelta) {
        super.render(context, mouseX, mouseY, tickDelta);
        content.render(context, mouseX, mouseY, tickDelta);
    }

    static Function<AbstractSlider<Float>, Component> formatVolume(String key) {
        return slider -> Component.translatable(key, (int)Math.floor(slider.getValue()));
    }
}
