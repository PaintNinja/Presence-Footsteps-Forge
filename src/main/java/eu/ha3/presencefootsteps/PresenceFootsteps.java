package eu.ha3.presencefootsteps;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import com.minelittlepony.common.client.gui.GameGui;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import eu.ha3.presencefootsteps.sound.SoundEngine;
import eu.ha3.presencefootsteps.util.Edge;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

@Mod(PresenceFootsteps.MODID)
public class PresenceFootsteps {
    public static final Logger logger = LogManager.getLogger("PFSolver");

    static final String MODID = "presencefootsteps";
    private static final KeyMapping.Category KEY_BINDING_CATEGORY = KeyMapping.Category.register(id("category"));

    public static final Component MOD_NAME = Component.translatable("mod.presencefootsteps.name");

    public static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(MODID, name);
    }

    private static PresenceFootsteps instance;

    public static PresenceFootsteps getInstance() {
        return instance;
    }

    private final Path pfFolder = FMLPaths.GAMEDIR.get().resolve("presencefootsteps");
    private final PFConfig config = new PFConfig(pfFolder.resolve("userconfig.json"), this);
    private final SoundEngine engine = new SoundEngine(config);
    private final PFDebugHud debugHud = new PFDebugHud(engine);
    private @Nullable Lazy<KeyMapping> optionsKeyBinding = null;
    private @Nullable Lazy<KeyMapping> toggleKeyBinding = null;
    private @Nullable Lazy<KeyMapping> debugToggleKeyBinding = null;
    private final Edge toggler = new Edge(z -> {
        if (z) {
            config.toggleDisabled();
        }
    });
    private final Edge debugToggle = new Edge(z -> {
        if (z) {
            Minecraft.getInstance().debugEntries.toggleStatus(PFDebugHud.ID);
        }
    });

    private final AtomicBoolean configChanged = new AtomicBoolean();

    public PresenceFootsteps() {
        instance = this;
        onInitializeClient();
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

    public void onInitializeClient() {
        config.load();
        config.onChangedExternally(c -> configChanged.set(true));

        RegisterKeyMappingsEvent.BUS.addListener(event -> {
            this.optionsKeyBinding = Lazy.of(() -> new KeyMapping("key.presencefootsteps.settings", InputConstants.Type.KEYSYM, InputConstants.KEY_F10, KEY_BINDING_CATEGORY));
            this.toggleKeyBinding = Lazy.of(() -> new KeyMapping("key.presencefootsteps.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, KEY_BINDING_CATEGORY));
            this.debugToggleKeyBinding = Lazy.of(() -> new KeyMapping("key.presencefootsteps.debug_toggle", InputConstants.Type.KEYSYM, InputConstants.KEY_Z, KEY_BINDING_CATEGORY));

            event.register(optionsKeyBinding.get());
            event.register(toggleKeyBinding.get());
            event.register(debugToggleKeyBinding.get());
        });

        TickEvent.ClientTickEvent.Post.BUS.addListener(event -> onTick(Minecraft.getInstance()));
        RegisterClientReloadListenersEvent.BUS.addListener(event -> event.registerReloadListener(engine));
//        DebugScreenEntries.register(PFDebugHud.ID, debugHud);
    }

    private void onTick(Minecraft client) {
        if (client.screen instanceof PFOptionsScreen screen && configChanged.getAndSet(false)) {
            screen.init(screen.width, screen.height);
        }

        debugToggle.accept(GameGui.isKeyDown(InputConstants.KEY_F3) && debugToggleKeyBinding.get().isDown());

        Optional.ofNullable(client.player).filter(e -> !e.isRemoved()).ifPresent(cameraEntity -> {
            if (client.screen == null) {
                if (optionsKeyBinding.get().isDown()) {
                    client.setScreen(new PFOptionsScreen(client.screen));
                }
                toggler.accept(toggleKeyBinding.get().isDown());
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
