package eu.ha3.presencefootsteps.sound;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import eu.ha3.presencefootsteps.PFConfig;
import eu.ha3.presencefootsteps.mixins.IEntity;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticsJsonParser;
import eu.ha3.presencefootsteps.sound.generator.Locomotion;
import eu.ha3.presencefootsteps.util.PlayerUtil;
import eu.ha3.presencefootsteps.util.ResourceUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;

public class SoundEngine implements PreparableReloadListener {
    private static final ResourceLocation BLOCK_MAP = new ResourceLocation("presencefootsteps", "config/blockmap.json");
    private static final ResourceLocation GOLEM_MAP = new ResourceLocation("presencefootsteps", "config/golemmap.json");
    private static final ResourceLocation LOCOMOTION_MAP = new ResourceLocation("presencefootsteps", "config/locomotionmap.json");
    private static final ResourceLocation PRIMITIVE_MAP = new ResourceLocation("presencefootsteps", "config/primitivemap.json");
    private static final ResourceLocation ACOUSTICS = new ResourceLocation("presencefootsteps", "config/acoustics.json");
    private static final ResourceLocation VARIATOR = new ResourceLocation("presencefootsteps", "config/variator.json");

    //private static final ResourceLocation ID = new ResourceLocation("presencefootsteps", "sounds");

    private PFIsolator isolator = new PFIsolator(this);

    private final PFConfig config;

    private boolean hasConfigurations;

    public SoundEngine(PFConfig config) {
        this.config = config;
    }

    public float getVolumeForSource(LivingEntity source) {
        float volume = config.getGlobalVolume() / 100F;

        if (source instanceof Player) {
            if (PlayerUtil.isClientPlayer(source)) {
                volume *= config.getClientPlayerVolume() / 100F;
            } else {
                volume *= config.getOtherPlayerVolume() / 100F;
            }
        }

        float runningProgress = ((StepSoundSource) source).getStepGenerator(this)
                .map(generator -> generator.getMotionTracker().getSpeedScalingRatio(source))
                .orElse(0F);

        return volume * (1F + ((config.getRunningVolumeIncrease() / 100F) * runningProgress));
    }

    public Isolator getIsolator() {
        return isolator;
    }

    public void reload() {
        if (config.getEnabled()) {
            reloadEverything(Minecraft.getInstance().getResourceManager());
        } else {
            shutdown();
        }
    }

    public boolean isRunning(Minecraft client) {
        return hasConfigurations && config.getEnabled() && (client.isLocalServer() || config.getEnabledMP());
    }

    private Stream<? extends Entity> getTargets(final Entity cameraEntity) {
        final List<? extends Entity> entities = cameraEntity.level().getEntities((Entity) null, cameraEntity.getBoundingBox().inflate(16), e -> {
            return e instanceof LivingEntity
                    && !(e instanceof WaterAnimal)
                    && !(e instanceof FlyingMob)
                    && !(e instanceof Shulker
                            || e instanceof ArmorStand
                            || e instanceof Boat
                            || e instanceof AbstractMinecart)
                        && !isolator.getGolemMap().contains(e.getType())
                        && !e.isPassenger()
                        && !((LivingEntity)e).isSleeping()
                        && (!(e instanceof Player) || !e.isSpectator())
                        && e.distanceToSqr(cameraEntity) <= 256
                        && config.getEntitySelector().test(e);
        });

        final Comparator<Entity> nearest = Comparator.comparingDouble(e -> e.distanceToSqr(cameraEntity));

        if (entities.size() < config.getMaxSteppingEntities()) {
            return entities.stream();
        }
        IntSet alreadyVisited = new IntOpenHashSet();
        return entities.stream()
            .sorted(nearest)
                    // Always play sounds for players and the entities closest to the camera
                        // If multiple entities share the same block, only play sounds for one of each distinct type
            .filter(e -> e == cameraEntity || e instanceof Player || (alreadyVisited.size() < config.getMaxSteppingEntities() && alreadyVisited.add(getVisitKey(e.getType(), e.blockPosition()))));
    }

    // Same value as Objects.hash(type, pos) without the varargs array and the boxed key
    private static int getVisitKey(EntityType<?> type, BlockPos pos) {
        return 31 * (31 + Objects.hashCode(type)) + Objects.hashCode(pos);
    }

    public void onFrame(Minecraft client, Entity cameraEntity) {
        if (!client.isPaused() && isRunning(client)) {
            getTargets(cameraEntity).forEach(e -> {
                try {
                    ((StepSoundSource) e).getStepGenerator(this).ifPresent(generator -> {
                        generator.setIsolator(isolator);
                        if (generator.generateFootsteps((LivingEntity)e)) {
                            ((IEntity) e).setNextStepDistance(Integer.MAX_VALUE);
                        } else if (((IEntity) e).getNextStepDistance() == Integer.MAX_VALUE) {
                            ((IEntity) e).setNextStepDistance(e.moveDist + 1);
                        }
                    });
                } catch (Throwable t) {
                    CrashReport report = CrashReport.forThrowable(t, "Generating PF sounds for entity");
                    CrashReportCategory section = report.addCategory("Entity being ticked");
                    if (e == null) {
                        section.setDetail("Entity Type", "null");
                    } else {
                        e.fillCrashReportCategory(section);
                        section.setDetail("Entity's Locomotion Type", isolator.getLocomotionMap().lookup(e));
                        section.setDetail("Entity is Golem", isolator.getGolemMap().contains(e.getType()));
                    }
                    config.populateCrashReport(report.addCategory("PF Configuration"));
                    throw new ReportedException(report);
                }
            });

            isolator.getSoundPlayer().think(); // Delayed sounds
        }
    }

    public boolean onSoundRecieved(@Nullable Holder<SoundEvent> event, SoundSource category) {
        if (event == null || category != SoundSource.PLAYERS || !isRunning(Minecraft.getInstance())) {
            return false;
        }

        return event.unwrap().right().filter(sound -> {
            if (event == SoundEvents.PLAYER_SWIM
                    || event == SoundEvents.PLAYER_SPLASH
                    || event == SoundEvents.PLAYER_BIG_FALL
                    || event == SoundEvents.PLAYER_SMALL_FALL) {
                       return true;
                   }

                   //String[] name = sound.getId().getPath().split("\\.");
                   return false;//name.length > 0
                          // && "block".contentEquals(name[0])
                          // && "step".contentEquals(name[name.length - 1]);
        }).isPresent();
    }

    public Locomotion getLocomotion(LivingEntity entity) {
        if (entity instanceof Player) {
            return Locomotion.forPlayer((Player)entity, config.getLocomotion());
        }
        return isolator.getLocomotionMap().lookup(entity);
    }

    @Override
    public @NotNull CompletableFuture<Void> reload(PreparationBarrier sync, ResourceManager sender,
                                                   ProfilerFiller serverProfiler, ProfilerFiller clientProfiler,
                                                   Executor serverExecutor, Executor clientExecutor) {
        return sync.wait(null).thenRunAsync(() -> {
            clientProfiler.startTick();
            clientProfiler.push("Reloading PF Sounds");
            reloadEverything(sender);
            clientProfiler.pop();
            clientProfiler.endTick();
        }, clientExecutor);
    }

    public void reloadEverything(ResourceManager manager) {
        isolator = new PFIsolator(this);
        hasConfigurations = false;

        hasConfigurations = ResourceUtils.forEachReverse(BLOCK_MAP, manager, isolator.getBlockMap()::load);
        hasConfigurations |= ResourceUtils.forEach(GOLEM_MAP, manager, isolator.getGolemMap()::load);
        hasConfigurations |= ResourceUtils.forEach(PRIMITIVE_MAP, manager, isolator.getPrimitiveMap()::load);
        hasConfigurations |= ResourceUtils.forEach(LOCOMOTION_MAP, manager, isolator.getLocomotionMap()::load);
        hasConfigurations |= ResourceUtils.forEach(ACOUSTICS, manager, new AcousticsJsonParser(isolator.getAcoustics())::parse);
        hasConfigurations |= ResourceUtils.forEach(VARIATOR, manager, isolator.getVariator()::load);
    }

    public void shutdown() {
        isolator = new PFIsolator(this);
        hasConfigurations = false;

        Player player = Minecraft.getInstance().player;

        if (player != null) {
            ((IEntity) player).setNextStepDistance(0);
        }
    }
}
