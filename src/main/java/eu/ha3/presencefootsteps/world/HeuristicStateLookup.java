package eu.ha3.presencefootsteps.world;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class HeuristicStateLookup {
    private final Function<Block, Optional<Block>> leafBlockCache = Util.memoize(block -> {
        return Stream.of(BuiltInRegistries.BLOCK.getKey(block).getPath())
            .flatMap(id -> Arrays.stream(id.split("_")))
            .flatMap(part -> BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse(part + "_leaves")).stream())
            .findFirst();
    });

    @Nullable
    public Block getMostSimilar(Block block) {
        if (block.defaultBlockState().getSoundType().getStepSound() == SoundEvents.GRASS_STEP) {
            return leafBlockCache.apply(block).orElse(null);
        }
        return null;
    }
}
