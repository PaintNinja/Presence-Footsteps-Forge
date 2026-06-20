package eu.ha3.presencefootsteps.config;

import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

public enum EntitySelector implements Predicate<Entity> {
    ALL(SoundSource.PLAYERS, SoundSource.HOSTILE, SoundSource.NEUTRAL) {
        @Override
        public boolean test(Entity e) {
            return true;
        }
    },
    PLAYERS_AND_HOSTILES(SoundSource.PLAYERS, SoundSource.HOSTILE) {
        @Override
        public boolean test(Entity e) {
            return e instanceof Player || e instanceof Enemy;
        }
    },
    PLAYERS_ONLY(SoundSource.PLAYERS) {
        @Override
        public boolean test(Entity e) {
            return e instanceof Player;
        }
    };

    public static final EntitySelector[] VALUES = values();

    private final Set<SoundSource> affectedSources;

    EntitySelector(SoundSource...sources) {
        affectedSources = Set.of(sources);
    }

    public Set<SoundSource> getAffectedSources() {
        return affectedSources;
    }
}
