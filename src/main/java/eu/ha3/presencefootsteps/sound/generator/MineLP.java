package eu.ha3.presencefootsteps.sound.generator;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;

public class MineLP {
    private static boolean checkCompleted = false;
    private static boolean hasMineLP;

    public static boolean hasPonies() {
        if (!checkCompleted) {
            checkCompleted = true;
            hasMineLP = ModList.get().isLoaded("minelp");
        }

        return hasMineLP;
    }

    public static Locomotion getLocomotion(Entity entity, Locomotion fallback) {
        return fallback;
    }

    public static Locomotion getLocomotion(Player ply) {
        return getLocomotion(ply, Locomotion.BIPED);
    }
}
