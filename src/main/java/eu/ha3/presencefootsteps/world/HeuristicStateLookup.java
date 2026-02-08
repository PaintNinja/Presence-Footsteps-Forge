package eu.ha3.presencefootsteps.world;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class HeuristicStateLookup {
    private final Function<Block, Optional<Block>> leafBlockCache = Util.memoize(block -> {
        String id = BuiltInRegistries.BLOCK.getKey(block).getPath();

        for (String part : id.split("_")) {
            Optional<Block> leavesBlock = BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse(part + "_leaves"));
            if (leavesBlock.isPresent()) {
                return leavesBlock;
            }
        }

        return Optional.empty();
    });

    @Nullable
    public Block getMostSimilar(Block block) {
        if (block.defaultBlockState().getSoundType().getStepSound() == SoundEvents.GRASS_STEP) {
            return leafBlockCache.apply(block).orElse(null);
        }
        return null;
    }
}
