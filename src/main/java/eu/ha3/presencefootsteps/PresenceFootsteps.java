package eu.ha3.presencefootsteps;

import eu.ha3.presencefootsteps.sound.SoundEngine;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.util.Lazy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Mod(PresenceFootsteps.MOD_ID)
public class PresenceFootsteps {
    public static final Logger logger = LogManager.getLogger("PFSolver");

    public static final String MOD_ID = "presencefootsteps";

    public static final Component MOD_NAME = Component.translatable("mod.presencefootsteps.name");

    private static PresenceFootsteps instance;

    public static PresenceFootsteps getInstance() {
        return instance;
    }

    public SoundEngine engine;

    public PFConfig config;

    @Nullable
    public Lazy<KeyMapping> keyBinding = null;

    public PresenceFootsteps() {
        instance = this;
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
        client.getToasts().addToast(SystemToast.multiline(client, SystemToast.SystemToastId.PACK_LOAD_FAILURE, title, body));
    }
}