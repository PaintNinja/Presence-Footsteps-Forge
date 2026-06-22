package eu.ha3.presencefootsteps.config;

import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;

public enum EntitySelector implements Predicate<Entity> {
    ALL(SoundCategory.PLAYERS, SoundCategory.HOSTILE, SoundCategory.NEUTRAL) {
        @Override
        public boolean test(Entity e) {
            return true;
        }
    },
    PLAYERS_AND_HOSTILES(SoundCategory.PLAYERS, SoundCategory.HOSTILE) {
        @Override
        public boolean test(Entity e) {
            return e instanceof PlayerEntity || e instanceof Monster;
        }
    },
    PLAYERS_ONLY(SoundCategory.PLAYERS) {
        @Override
        public boolean test(Entity e) {
            return e instanceof PlayerEntity;
        }
    };

    public static final EntitySelector[] VALUES = values();

    private final Set<SoundCategory> affectedSources;

    EntitySelector(SoundCategory...sources) {
        affectedSources = Set.of(sources);
    }

    public Set<SoundCategory> getAffectedSources() {
        return affectedSources;
    }
}
