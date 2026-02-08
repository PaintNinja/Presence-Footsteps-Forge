package eu.ha3.presencefootsteps;

import java.nio.file.Path;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import com.minelittlepony.common.util.GamePaths;
import com.mojang.blaze3d.platform.InputConstants;
import eu.ha3.presencefootsteps.sound.SoundEngine;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

public class PresenceFootsteps implements ClientModInitializer {
    public static final Logger logger = LogManager.getLogger("PFSolver");

    private static final String MODID = "presencefootsteps";
    private static final String KEY_BINDING_CATEGORY = "key.category." + MODID;

    public static final Component MOD_NAME = Component.translatable("mod.presencefootsteps.name");

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MODID, name);
    }

    private static PresenceFootsteps instance;

    public static PresenceFootsteps getInstance() {
        return instance;
    }

    private SoundEngine engine;

    private PFConfig config;

    private PFDebugHud debugHud;

    private KeyMapping optionsKeyBinding;
    private KeyMapping toggleKeyBinding;
    private boolean toggleTriggered;

    public PresenceFootsteps() {
        instance = this;
    }

    public PFDebugHud getDebugHud() {
        return debugHud;
    }

    public SoundEngine getEngine() {
        return engine;
    }

    public PFConfig getConfig() {
        return config;
    }

    public KeyMapping getOptionsKeyBinding() {
        return optionsKeyBinding;
    }

    @Override
    public void onInitializeClient() {
        Path pfFolder = GamePaths.getConfigDirectory().resolve("presencefootsteps");

        config = new PFConfig(pfFolder.resolve("userconfig.json"), this);
        config.load();

        optionsKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.presencefootsteps.settings", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F10, KEY_BINDING_CATEGORY));
        toggleKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.presencefootsteps.toggle", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KEY_BINDING_CATEGORY));

        engine = new SoundEngine(config);
        debugHud = new PFDebugHud(engine);

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(engine);
    }

    private void onTick(Minecraft client) {
        Optional.ofNullable(client.player).filter(e -> !e.isRemoved()).ifPresent(cameraEntity -> {
            if (client.screen == null) {
                if (optionsKeyBinding.isDown()) {
                    client.setScreen(new PFOptionsScreen(client.screen));
                }
                if (toggleKeyBinding.isDown()) {
                    if (!toggleTriggered) {
                        toggleTriggered = true;
                        config.toggleDisabled();
                    }
                } else {
                    toggleTriggered = false;
                }
            }

            engine.onFrame(client, cameraEntity);
        });
    }

    void onEnabledStateChange(boolean enabled) {
        engine.reload();
        showSystemToast(
                MOD_NAME,
                Component.translatable("key.presencefootsteps.toggle." + (enabled ? "enabled" : "disabled")).withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.GRAY)
        );
    }

    public void showSystemToast(Component title, Component body) {
        Minecraft client = Minecraft.getInstance();
        client.getToastManager().addToast(SystemToast.multiline(client, SystemToast.SystemToastId.PACK_LOAD_FAILURE, title, body));
    }
}
