package eu.ha3.presencefootsteps.world;

import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

abstract class AbstractSubstrateLookup<T> implements Lookup<T> {
    private final Map<String, Map<ResourceLocation, SoundsKey>> substrates = new Object2ObjectLinkedOpenHashMap<>();

    protected abstract ResourceLocation getId(T key);

    @Override
    public SoundsKey getAssociation(T key, String substrate) {
        final ResourceLocation id = getId(key);
        return getSubstrateMap(id, substrate).getOrDefault(id, SoundsKey.UNASSIGNED);
    }

    @Nullable
    protected Map<ResourceLocation, SoundsKey> getSubstrateMap(ResourceLocation id, String substrate) {
        Map<ResourceLocation, SoundsKey> primitives = substrates.get(substrate);
        if (primitives != null) {
            return primitives;
        }

        // check for break sound
        primitives = substrates.get("break_" + id.getPath());

        if (primitives != null) {
            return primitives;
        }

        // Check for default
        return substrates.getOrDefault(Substrates.DEFAULT, Map.of());
    }

    @Override
    public Set<String> getSubstrates() {
        return substrates.keySet();
    }

    @Override
    public void add(String key, String value) {
        final String[] split = key.trim().split("@");
        final String primitive = split[0];
        final String substrate = split.length > 1 ? split[1] : Substrates.DEFAULT;

        substrates
            .computeIfAbsent(substrate, s -> new Object2ObjectLinkedOpenHashMap<>())
            .put(ResourceLocation.parse(primitive), SoundsKey.of(value));
    }

    @Override
    public boolean contains(T key) {
        final ResourceLocation primitive = getId(key);

        for (var primitives : substrates.values()) {
            if (primitives.containsKey(primitive)) {
                return true;
            }
        }
        return false;
    }
}
