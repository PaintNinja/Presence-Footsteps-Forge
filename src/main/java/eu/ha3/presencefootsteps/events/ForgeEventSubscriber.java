package eu.ha3.presencefootsteps.events;

import eu.ha3.presencefootsteps.PresenceFootsteps;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = PresenceFootsteps.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventSubscriber {
    private static final PresenceFootsteps presenceFootsteps = PresenceFootsteps.getInstance();

    @SubscribeEvent
    public static void onClientTick(final TickEvent.ClientTickEvent.Post event) {
        final Minecraft client = Minecraft.getInstance();
        Optional.ofNullable(client.player).filter(e -> !e.isRemoved()).ifPresent(cameraEntity -> {
            // TODO: GUIs
//            if (client.screen == null && presenceFootsteps.keyBinding.get().isDown()) {
//                client.setScreen(new PFOptionsScreen(client.screen));
//            }

            presenceFootsteps.engine.onFrame(client, cameraEntity);
        });
    }
}
