package eu.ha3.presencefootsteps.sound;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import eu.ha3.presencefootsteps.PresenceFootsteps;
import eu.ha3.presencefootsteps.config.Variator;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticLibrary;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticsFile;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticsPlayer;
import eu.ha3.presencefootsteps.sound.generator.Locomotion;
import eu.ha3.presencefootsteps.sound.player.DelayedSoundPlayer;
import eu.ha3.presencefootsteps.util.JsonObjectWriter;
import eu.ha3.presencefootsteps.util.ResourceUtils;
import eu.ha3.presencefootsteps.util.BlockReport.Reportable;
import eu.ha3.presencefootsteps.world.BiomeVarianceLookup;
import eu.ha3.presencefootsteps.world.GolemLookup;
import eu.ha3.presencefootsteps.world.HeuristicStateLookup;
import eu.ha3.presencefootsteps.world.Index;
import eu.ha3.presencefootsteps.world.LocomotionLookup;
import eu.ha3.presencefootsteps.world.Lookup;
import eu.ha3.presencefootsteps.world.PrimitiveLookup;
import eu.ha3.presencefootsteps.world.StateLookup;

public record Isolator (
        Variator variator,
        Index<Entity, Locomotion> locomotions,
        HeuristicStateLookup heuristics,
        Lookup<EntityType<?>> golems,
        Lookup<BlockState> globalBlocks,
        Map<EntityType<?>, Lookup<BlockState>> blocks,
        Index<ResourceLocation, BiomeVarianceLookup.BiomeVariance> biomes,
        Lookup<SoundEvent> primitives,
        AcousticLibrary acoustics
    ) implements Reportable {
    private static final ResourceLocation BLOCK_MAP = PresenceFootsteps.id("config/blockmap.json");
    private static final ResourceLocation BIOME_MAP = PresenceFootsteps.id("config/biomevariancemap.json");
    private static final ResourceLocation GOLEM_MAP = PresenceFootsteps.id("config/golemmap.json");
    private static final ResourceLocation LOCOMOTION_MAP = PresenceFootsteps.id("config/locomotionmap.json");
    private static final ResourceLocation PRIMITIVE_MAP = PresenceFootsteps.id("config/primitivemap.json");
    public static final ResourceLocation ACOUSTICS = PresenceFootsteps.id("config/acoustics.json");
    private static final ResourceLocation VARIATOR = PresenceFootsteps.id("config/variator.json");

    public Isolator(SoundEngine engine) {
        this(new Variator(),
                new LocomotionLookup(engine.getConfig()),
                new HeuristicStateLookup(),
                new Lookup<>(),
                new Lookup<>(),
                new HashMap<>(),
                new BiomeVarianceLookup(),
                new Lookup<>(),
                new AcousticsPlayer(new DelayedSoundPlayer(engine.soundPlayer))
        );
    }

    public Lookup<BlockState> blocks(EntityType<?> sourceType) {
        if (sourceType == EntityType.PLAYER) {
            return globalBlocks();
        }
        return blocks.getOrDefault(sourceType, globalBlocks());
    }

    public boolean load(ResourceManager manager) {
        boolean hasConfigurations = false;
        hasConfigurations |= globalBlocks().load(ResourceUtils.load(BLOCK_MAP, manager, StateLookup::new));

        blocks.clear();
        blocks.putAll(ResourceUtils.loadDir(FileToIdConverter.json("config/blockmaps/entity"), manager, StateLookup::new, id -> {
            return BuiltInRegistries.ENTITY_TYPE.getOptional(id.withPath(p -> p.replace("config/blockmaps/entity/", "").replace(".json", ""))).orElse(null);
        }, entries -> {
            Lookup<BlockState> lookup = new Lookup<>();
            return lookup.load(entries, globalBlocks()) ? lookup : null;
        }));
        hasConfigurations |= !blocks.isEmpty();
        hasConfigurations |= ResourceUtils.forEach(BIOME_MAP, manager, biomes()::load);
        hasConfigurations |= golems().load(ResourceUtils.load(GOLEM_MAP, manager, GolemLookup::new));
        hasConfigurations |= primitives().load(ResourceUtils.load(PRIMITIVE_MAP, manager, PrimitiveLookup::new));
        hasConfigurations |= ResourceUtils.forEach(LOCOMOTION_MAP, manager, locomotions()::load);
        hasConfigurations |= ResourceUtils.forEach(ACOUSTICS, manager, reader -> AcousticsFile.read(reader, acoustics()::addAcoustic, false));
        hasConfigurations |= ResourceUtils.forEach(VARIATOR, manager, variator()::load);
        return hasConfigurations;
    }

    @Override
    public void writeToReport(boolean full, JsonObjectWriter writer, Map<String, SoundType> groups) throws IOException {
        writer.object(() -> {
            writer.object("blocks", () -> StateLookup.writeToReport(globalBlocks(), full, writer, groups));
            writer.object("golems", () -> GolemLookup.writeToReport(golems(), full, writer, groups));
            writer.object("entities", () -> locomotions().writeToReport(full, writer, groups));
            writer.object("primitives", () -> PrimitiveLookup.writeToReport(primitives(), full, writer, groups));
        });
    }
}
