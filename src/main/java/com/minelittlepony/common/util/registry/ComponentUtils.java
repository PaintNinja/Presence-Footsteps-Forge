package com.minelittlepony.common.util.registry;

import net.minecraft.core.component.DataComponentPatch;

/**
 * Utilities for working with components
 */
public interface ComponentUtils {
    /**
     * Returns a mutable copy of a data component patch
     */
    static DataComponentPatch.Builder copy(DataComponentPatch patch) {
        return copy(patch, DataComponentPatch.builder());
    }

    /**
     * Applies the contents of a patch onto a builder.
     */
    static DataComponentPatch.Builder copy(DataComponentPatch from, DataComponentPatch.Builder to) {
        var changes = from.split();
        changes.added().forEach(to::set);
        changes.removed().forEach(to::remove);
        return to;
    }
}
