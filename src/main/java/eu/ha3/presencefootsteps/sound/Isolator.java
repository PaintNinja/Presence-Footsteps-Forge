package eu.ha3.presencefootsteps.sound;

import java.io.IOException;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import eu.ha3.presencefootsteps.config.Variator;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticLibrary;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticsFile;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticsPlayer;
import eu.ha3.presencefootsteps.sound.generator.Locomotion;
import eu.ha3.presencefootsteps.sound.player.DelayedSoundPlayer;
import eu.ha3.presencefootsteps.util.JsonObjectWriter;
import eu.ha3.presencefootsteps.util.ResourceUtils;
import eu.ha3.presencefootsteps.util.BlockReport.Reportable;
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
        Lookup<BlockState> blocks,
        Lookup<SoundEvent> primitives,
        AcousticLibrary acoustics
    ) implements Reportable {
    private static final ResourceLocation BLOCK_MAP = ResourceLocation.fromNamespaceAndPath("presencefootsteps", "config/blockmap.json");
    private static final ResourceLocation GOLEM_MAP = ResourceLocation.fromNamespaceAndPath("presencefootsteps", "config/golemmap.json");
    private static final ResourceLocation LOCOMOTION_MAP = ResourceLocation.fromNamespaceAndPath("presencefootsteps", "config/locomotionmap.json");
    private static final ResourceLocation PRIMITIVE_MAP = ResourceLocation.fromNamespaceAndPath("presencefootsteps", "config/primitivemap.json");
    public static final ResourceLocation ACOUSTICS = ResourceLocation.fromNamespaceAndPath("presencefootsteps", "config/acoustics.json");
    private static final ResourceLocation VARIATOR = ResourceLocation.fromNamespaceAndPath("presencefootsteps", "config/variator.json");

    public Isolator(SoundEngine engine) {
        this(new Variator(),
                new LocomotionLookup(engine.getConfig()),
                new HeuristicStateLookup(),
                new GolemLookup(),
                new StateLookup(),
                new PrimitiveLookup(),
                new AcousticsPlayer(new DelayedSoundPlayer(engine.soundPlayer))
        );
    }

    public boolean load(ResourceManager manager) {
        boolean hasConfigurations = false;
        hasConfigurations |= ResourceUtils.forEachReverse(BLOCK_MAP, manager, blocks()::load);
        hasConfigurations |= ResourceUtils.forEach(GOLEM_MAP, manager, golems()::load);
        hasConfigurations |= ResourceUtils.forEach(PRIMITIVE_MAP, manager, primitives()::load);
        hasConfigurations |= ResourceUtils.forEach(LOCOMOTION_MAP, manager, locomotions()::load);
        hasConfigurations |= ResourceUtils.forEach(ACOUSTICS, manager, reader -> AcousticsFile.read(reader, acoustics()::addAcoustic, false));
        hasConfigurations |= ResourceUtils.forEach(VARIATOR, manager, variator()::load);
        return hasConfigurations;
    }

    @Override
    public void writeToReport(boolean full, JsonObjectWriter writer, Map<String, SoundType> groups) throws IOException {
        writer.object(() -> {
            writer.object("blocks", () -> blocks().writeToReport(full, writer, groups));
            writer.object("entities", () -> locomotions().writeToReport(full, writer, groups));
            writer.object("primitives", () -> primitives().writeToReport(full, writer, groups));
        });
    }
}
