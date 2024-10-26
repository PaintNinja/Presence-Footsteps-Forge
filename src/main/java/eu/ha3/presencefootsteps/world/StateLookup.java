package eu.ha3.presencefootsteps.world;

import eu.ha3.presencefootsteps.PresenceFootsteps;
import eu.ha3.presencefootsteps.util.JsonObjectWriter;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A state lookup that finds an association for a given block state within a specific substrate (or no substrate).
 *
 * @author Sollace
 */
public record StateLookup(Map<String, Bucket> substrates) implements Lookup<BlockState> {

    public StateLookup() {
        this(new Object2ObjectLinkedOpenHashMap<>());
    }

    @Override
    public SoundsKey getAssociation(BlockState state, String substrate) {
        return substrates.getOrDefault(substrate, Bucket.EMPTY).get(state).value;
    }

    @Override
    public void add(String key, String value) {
        SoundsKey sound = SoundsKey.of(value);
        if (!sound.isResult()) {
            return;
        }

        Key k = Key.of(key, sound);

        substrates.computeIfAbsent(k.substrate, Bucket.Substrate::new).add(k);
    }

    @Override
    public Set<String> getSubstrates() {
        return substrates.keySet();
    }

    @Override
    public boolean contains(BlockState state) {
        for (Bucket substrate : substrates.values()) {
            if (substrate.contains(state)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void writeToReport(boolean full, JsonObjectWriter writer, Map<String, SoundType> groups) throws IOException {
        writer.each(BuiltInRegistries.BLOCK, block -> {
            BlockState state = block.defaultBlockState();

            var group = block.defaultBlockState().getSoundType();
            if (group != null && group.getStepSound() != null) {
                String substrate = String.format(Locale.ENGLISH, "%.2f_%.2f", group.volume, group.pitch);
                groups.put(group.getStepSound().getLocation().toString() + "@" + substrate, group);
            }

            if (full || !contains(state)) {
                writer.object(BuiltInRegistries.BLOCK.getKey(block).toString(), () -> {
                    writer.field("class", getClassData(state));
                    writer.field("tags", getTagData(state));
                    writer.field("sound", getSoundData(group));
                    writer.object("associations", () -> {
                        getSubstrates().forEach(substrate -> {
                            try {
                                SoundsKey association = getAssociation(state, substrate);
                                if (association.isResult()) {
                                    writer.field(substrate, association.raw());
                                }
                            } catch (IOException ignore) {
                            }
                        });
                    });
                });
            }
        });
    }

    private String getSoundData(@Nullable SoundType group) {
        if (group == null) {
            return "NULL";
        }
        if (group.getStepSound() == null) {
            return "NO_SOUND";
        }
        return group.getStepSound().getLocation().getPath();
    }

    private String getClassData(BlockState state) {
        @Nullable
        String canonicalName = state.getBlock().getClass().getCanonicalName();
        if (canonicalName == null) {
            return "<anonymous>";
        }

        return canonicalName;
    }

    private String getTagData(BlockState state) {
        return BuiltInRegistries.BLOCK.getTagNames().filter(state::is).map(TagKey::location).map(ResourceLocation::toString).collect(Collectors.joining(","));
    }

    private interface Bucket {

        Bucket EMPTY = state -> Key.NULL;

        default void add(Key key) {
        }

        Key get(BlockState state);

        default boolean contains(BlockState state) {
            return false;
        }

        record Substrate(
            KeyList wildcards,
            Map<ResourceLocation, Bucket> blocks,
            Map<ResourceLocation, Bucket> tags) implements Bucket {

            Substrate(String substrate) {
                this(new KeyList(), new Object2ObjectLinkedOpenHashMap<>(), new Object2ObjectLinkedOpenHashMap<>());
            }

            @Override
            public void add(Key key) {
                if (key.isWildcard()) {
                    wildcards.add(key);
                } else {
                    (key.isTag() ? tags : blocks).computeIfAbsent(key.identifier(), Tile::new).add(key);
                }
            }

            @Override
            public Key get(BlockState state) {
                final Key association = getTile(state).get(state);

                return association == Key.NULL
                    ? wildcards.findMatch(state)
                    : association;
            }

            @Override
            public boolean contains(BlockState state) {
                return getTile(state).contains(state) || wildcards.findMatch(state) != Key.NULL;
            }

            private Bucket getTile(BlockState state) {
                return blocks.computeIfAbsent(BuiltInRegistries.BLOCK.getKey(state.getBlock()), id -> {
                    for (ResourceLocation tag : tags.keySet()) {
                        if (state.is(TagKey.create(Registries.BLOCK, tag))) {
                            return tags.get(tag);
                        }
                    }

                    return Bucket.EMPTY;
                });
            }
        }

        record Tile(Map<BlockState, Key> cache, KeyList keys) implements Bucket {
            Tile(ResourceLocation id) {
                this(new Object2ObjectLinkedOpenHashMap<>(), new KeyList());
            }

            @Override
            public void add(Key key) {
                keys.add(key);
            }

            @Override
            public Key get(BlockState state) {
                return cache.computeIfAbsent(state, keys::findMatch);
            }

            @Override
            public boolean contains(BlockState state) {
                return get(state) != Key.NULL;
            }
        }
    }

    private record KeyList(Set<Key> priorityKeys, Set<Key> keys) {

        public KeyList() {
            this(new ObjectLinkedOpenHashSet<>(), new ObjectLinkedOpenHashSet<>());
        }

        void add(Key key) {
            Set<Key> keys = getSetFor(key);
            keys.remove(key);
            keys.add(key);
        }

        private Set<Key> getSetFor(Key key) {
            return key.empty() ? keys : priorityKeys;
        }

        public Key findMatch(BlockState state) {
            for (Key i : priorityKeys) {
                if (i.matches(state)) {
                    return i;
                }
            }
            for (Key i : keys) {
                if (i.matches(state)) {
                    return i;
                }
            }
            return Key.NULL;
        }
    }

    private record Key(
        ResourceLocation identifier,
        String substrate,
        Set<Attribute> properties,
        SoundsKey value,
        boolean empty,
        boolean isTag,
        boolean isWildcard
    ) {
        public static final Key NULL = new Key(ResourceLocation.withDefaultNamespace("air"), "", ObjectSets.emptySet(), SoundsKey.UNASSIGNED, true, false, false);

        public static Key of(String key, SoundsKey value) {
            final boolean isTag = key.indexOf('#') == 0;

            if (isTag) {
                key = key.replaceFirst("#", "");
            }

            final String id = key.split("[\\.\\[]")[0];
            final boolean isWildcard = id.indexOf('*') == 0;
            ResourceLocation identifier = ResourceLocation.withDefaultNamespace("air");

            if (!isWildcard) {
                if (id.indexOf('^') > -1) {
                    identifier = ResourceLocation.parse(id.split("\\^")[0]);
                    PresenceFootsteps.logger.warn("Metadata entry for " + key + "=" + value.raw() + " was ignored");
                } else {
                    identifier = ResourceLocation.parse(id);
                }

                if (!isTag && !BuiltInRegistries.BLOCK.containsKey(identifier)) {
                    PresenceFootsteps.logger.warn("Sound registered for unknown block id " + identifier);
                }
            }

            key = key.replace(id, "");
            final String substrate = key.replaceFirst("\\[[^\\]]+\\]", "");
            String finalSubstrate = "";

            if (substrate.indexOf('.') > -1) {
                finalSubstrate = substrate.split("\\.")[1];
                key = key.replace(substrate, "");
            }

            final Set<Attribute> properties = ObjectArrayList.of(
                    key.replace("[", "")
                        .replace("]", "")
                        .split(","))
                .stream()
                .filter(line -> line.indexOf('=') > -1)
                .map(Attribute::new)
                .collect(ObjectOpenHashSet.toSet());

            final boolean empty = properties.isEmpty();

            return new Key(identifier, finalSubstrate, properties, value, empty, isTag, isWildcard);
        }

        boolean matches(BlockState state) {
            if (empty) {
                return true;
            }

            Map<Property<?>, Comparable<?>> entries = state.getValues();
            Set<Property<?>> keys = entries.keySet();

            for (Attribute property : properties) {
                for (Property<?> key : keys) {
                    if (key.getName().equals(property.name)) {
                        Comparable<?> value = entries.get(key);

                        if (!Objects.toString(value).equalsIgnoreCase(property.value)) {
                            return false;
                        }
                    }
                }
            }

            return true;
        }

        @Override
        public String toString() {
            return (isTag ? "#" : "")
                + identifier
                + "[" + properties.stream().map(Attribute::toString).collect(Collectors.joining()) + "]"
                + "." + substrate
                + "=" + value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(empty, identifier, isTag, isWildcard, properties, substrate);
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || (obj != null && getClass() == obj.getClass()) && equals((Key) obj);
        }

        private boolean equals(Key other) {
            return isTag == other.isTag
                && isWildcard == other.isWildcard
                && empty == other.empty
                && Objects.equals(identifier, other.identifier)
                && Objects.equals(substrate, other.substrate)
                && Objects.equals(properties, other.properties);
        }

        private record Attribute(String name, String value) {
            Attribute(String prop) {
                this(prop.split("="));
            }

            Attribute(String[] split) {
                this(split[0], split[1]);
            }

            @Override
            public String toString() {
                return name + "=" + value;
            }
        }
    }
}