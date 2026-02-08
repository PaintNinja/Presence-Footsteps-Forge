package eu.ha3.presencefootsteps;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import eu.ha3.presencefootsteps.sound.SoundEngine;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@Mod(PresenceFootsteps.MODID)
public class PresenceFootsteps {
    public static final Logger logger = LogManager.getLogger("PFSolver");

    static final String MODID = "presencefootsteps";
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

    private @Nullable Lazy<KeyMapping> optionsKeyBinding = null;
    private @Nullable Lazy<KeyMapping> toggleKeyBinding = null;
    private boolean toggleTriggered;

    private final AtomicBoolean configChanged = new AtomicBoolean();

    public PresenceFootsteps(FMLJavaModLoadingContext ctx) {
        instance = this;
        onInitializeClient(ctx);
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
        return optionsKeyBinding.get();
    }

    public void onInitializeClient(FMLJavaModLoadingContext ctx) {
        Path pfFolder = FMLPaths.CONFIGDIR.get().resolve("presencefootsteps");

        config = new PFConfig(pfFolder.resolve("userconfig.json"), this);
        config.load();
        config.onChangedExternally(c -> configChanged.set(true));

        RegisterKeyMappingsEvent.getBus(ctx.getModBusGroup()).addListener(event -> {
            optionsKeyBinding = Lazy.of(() -> new KeyMapping("key.presencefootsteps.settings", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F10, KEY_BINDING_CATEGORY));
            toggleKeyBinding = Lazy.of(() -> new KeyMapping("key.presencefootsteps.toggle", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KEY_BINDING_CATEGORY));

            event.register(optionsKeyBinding.get());
            event.register(toggleKeyBinding.get());
        });

        engine = new SoundEngine(config);
        debugHud = new PFDebugHud(engine);

        TickEvent.ClientTickEvent.Post.BUS.addListener(event -> onTick(Minecraft.getInstance()));
        RegisterClientReloadListenersEvent.getBus(ctx.getModBusGroup()).addListener(event -> event.registerReloadListener(engine));
    }

    private void onTick(Minecraft client) {
        if (client.screen instanceof PFOptionsScreen screen && configChanged.getAndSet(false)) {
            screen.init(client, screen.width, screen.height);
        }

        Optional.ofNullable(client.player).filter(e -> !e.isRemoved()).ifPresent(cameraEntity -> {

            if (client.screen == null) {
                if (optionsKeyBinding.get().isDown()) {
                    client.setScreen(new PFOptionsScreen(client.screen));
                }
                if (toggleKeyBinding.get().isDown()) {
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
