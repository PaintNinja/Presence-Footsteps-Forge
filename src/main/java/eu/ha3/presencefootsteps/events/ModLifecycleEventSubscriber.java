package eu.ha3.presencefootsteps.events;

import com.mojang.blaze3d.platform.InputConstants;
import eu.ha3.presencefootsteps.PFConfig;
import eu.ha3.presencefootsteps.PresenceFootsteps;
import eu.ha3.presencefootsteps.sound.SoundEngine;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;

import static eu.ha3.presencefootsteps.PresenceFootsteps.logger;

@EventBusSubscriber(modid = PresenceFootsteps.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModLifecycleEventSubscriber {
    private static final PresenceFootsteps presenceFootsteps = PresenceFootsteps.getInstance();

    @SubscribeEvent
    public static void onConstruct(final FMLConstructModEvent event) {
        logger.info("Presence Footsteps starting");
    }

    @SubscribeEvent
    public static void onRegisterClientReloadListeners(final RegisterClientReloadListenersEvent event) {
        final Path pfFolder = FMLPaths.CONFIGDIR.get().resolve(PresenceFootsteps.MOD_ID);
        presenceFootsteps.config = new PFConfig(pfFolder.resolve("userconfig.json"), presenceFootsteps);
        presenceFootsteps.config.load();
        presenceFootsteps.engine = new SoundEngine(presenceFootsteps.config);
        event.registerReloadListener(presenceFootsteps.engine);
    }

    @SubscribeEvent
    public static void registerKeyBinding(final RegisterKeyMappingsEvent event) {
        presenceFootsteps.keyBinding = Lazy.of(() ->
                new KeyMapping("key.presencefootsteps.settings", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F10, "key.categories.misc"));

        event.register(presenceFootsteps.keyBinding.get());
    }
}
