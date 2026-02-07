package com.minelittlepony.common.util.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import java.lang.reflect.Type;

class SettingSerializer implements JsonSerializer<Setting<?>> {
    @SuppressWarnings("unchecked")
    @Override
    public JsonElement serialize(Setting<?> src, Type typeOfSrc, JsonSerializationContext context) {
        return src.getType().token().map(
                token -> context.serialize(src.get()),
                codec -> ((Codec<Object>)codec).encodeStart(JsonOps.INSTANCE, src.get()).result().orElseThrow()
        );
    }
}