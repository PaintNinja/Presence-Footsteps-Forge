package eu.ha3.presencefootsteps;

import java.util.*;
import java.util.Map.Entry;
import eu.ha3.presencefootsteps.api.DerivedBlock;
import eu.ha3.presencefootsteps.sound.SoundEngine;
import eu.ha3.presencefootsteps.sound.generator.Locomotion;
import eu.ha3.presencefootsteps.world.PrimitiveLookup;
import eu.ha3.presencefootsteps.world.SoundsKey;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class PFDebugHud {

    private final SoundEngine engine;

    private final List<String> list = new ArrayList<>();

    PFDebugHud(SoundEngine engine) {
        this.engine = engine;
    }

    public void render(HitResult blockHit, HitResult fluidHit, List<String> finalList) {
        Minecraft client = Minecraft.getInstance();

        list.add("");
        list.add(ChatFormatting.UNDERLINE + "Presence Footsteps " + FabricLoader.getInstance().getModContainer("presencefootsteps").get().getMetadata().getVersion());

        PFConfig config = engine.getConfig();
        list.add(String.format("Enabled: %s, Multiplayer: %s, Running: %s", config.getEnabled(), config.getEnabledMP(), engine.isRunning(client)));
        list.add(String.format("Volume: Global[G: %s%%, W: %s%%, F: %s%%]",
                config.getGlobalVolume(),
                config.wetSoundsVolume,
                config.foliageSoundsVolume
        ));
        list.add(String.format("Entities[H: %s%%, P: %s%%], Players[U: %s%%, T: %s%% ]",
                config.hostileEntitiesVolume,
                config.passiveEntitiesVolume,
                config.clientPlayerVolume,
                config.otherPlayerVolume
        ));
        list.add(String.format("Stepping Mode: %s, Targeting Mode: %s, Footwear: %s", config.getLocomotion() == Locomotion.NONE
                ? String.format("AUTO (%sDETECTED %s%s)", ChatFormatting.BOLD, Locomotion.forPlayer(client.player, Locomotion.NONE), ChatFormatting.RESET)
                : config.getLocomotion(), config.getEntitySelector(), config.getEnabledFootwear()));
        list.add(String.format("Data Loaded: B%s P%s G%s",
                engine.getIsolator().globalBlocks().getSubstrates().size(),
                engine.getIsolator().primitives().getSubstrates().size(),
                engine.getIsolator().golems().getSubstrates().size()
        ));
        list.add(String.format("Has Resource Pack: %s%s", engine.hasData() ? ChatFormatting.GREEN : ChatFormatting.RED, engine.hasData()));

        insertAt(list, finalList, "Targeted Block: ", -1);

        if (blockHit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult)blockHit).getBlockPos();
            BlockState state = client.level.getBlockState(pos);

            list.add("");
            list.add(ChatFormatting.UNDERLINE + "Targeted Block Sounds Like");
            BlockState base = DerivedBlock.getBaseOf(state);
            if (!base.isAir()) {
                list.add(BuiltInRegistries.BLOCK.getKey(base.getBlock()).toString());
            }
            list.add(String.format(Locale.ENGLISH, "Primitive Key: %s", PrimitiveLookup.getKey(state.getSoundType())));
            BlockPos above = pos.above();
            boolean hasRain = client.level.isRaining() && client.level.getBiome(above).value().getPrecipitationAt(above, client.level.getSeaLevel()) == Biome.Precipitation.RAIN;
            boolean hasLava = client.level.getBlockState(above).getFluidState().is(FluidTags.LAVA);
            boolean hasWater = client.level.isRainingAt(above)
                    || state.getFluidState().is(FluidTags.WATER)
                    || client.level.getBlockState(above).getFluidState().is(FluidTags.WATER);
            list.add("Surface Condition: " + (
                    hasLava ? ChatFormatting.RED + "LAVA"
                            : hasWater ? ChatFormatting.BLUE + "WET"
                            : hasRain ? ChatFormatting.GRAY + "SHELTERED" : ChatFormatting.GRAY + "DRY"
            ));
            renderSoundList("Step Sounds[B]", engine.getIsolator().globalBlocks().getAssociations(state), list);
            renderSoundList("Step Sounds[P]", engine.getIsolator().primitives().getAssociations(state.getSoundType().getStepSound()), list);
            list.add("");

            insertAt(list, finalList, "Targeted Block: ", 1);
        }

        if (client.hitResult instanceof EntityHitResult ehr && ehr.getEntity() != null) {
            list.add(String.format("Targeted Entity Step Mode: %s", engine.getIsolator().locomotions().lookup(ehr.getEntity())));
            renderSoundList("Step Sounds[G]", engine.getIsolator().golems().getAssociations(ehr.getEntity().getType()), list);
            insertAt(list, finalList, "Targeted Entity", 3);
        }
    }

    private static void insertAt(List<String> values, List<String> destination, String target, int offset) {
        int i = 0;
        for (; i < destination.size(); i++) {
            if (destination.get(i).indexOf(target) != -1) {
                break;
            }
        }

        destination.addAll(Mth.clamp(i + offset, 0, destination.size()), values);
        values.clear();
    }

    private void renderSoundList(String title, Map<String, SoundsKey> sounds, List<String> list) {
        if (sounds.isEmpty()) {
            return;
        }
        StringBuilder combinedList = new StringBuilder(ChatFormatting.UNDERLINE + title + ChatFormatting.RESET + ": [ ");
        boolean first = true;
        for (var entry : sounds.entrySet()) {
            if (!first) {
                combinedList.append(" / ");
            }
            first = false;

            if (!entry.getKey().isEmpty()) {
                combinedList.append(entry.getKey()).append(":");
            }
            combinedList.append(entry.getValue().raw());
        }
        combinedList.append(" ]");
        list.add(combinedList.toString());

        if (!list.isEmpty()) {
            return;
        }

        if (sounds.isEmpty()) {
            list.add(SoundsKey.UNASSIGNED.raw());
        } else {
            sounds.forEach((key, value) -> {
                list.add((key.isEmpty() ? "default" : key) + ": " + value.raw());
            });
        }
    }
}
