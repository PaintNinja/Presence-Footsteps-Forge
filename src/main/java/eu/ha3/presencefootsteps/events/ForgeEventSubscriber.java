package eu.ha3.presencefootsteps.events;

import eu.ha3.presencefootsteps.PresenceFootsteps;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.Optional;

@EventBusSubscriber(modid = PresenceFootsteps.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ForgeEventSubscriber {
    private static final PresenceFootsteps presenceFootsteps = PresenceFootsteps.getInstance();

    @SubscribeEvent
    public static void onClientTick(final ClientTickEvent.Post event) {
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
