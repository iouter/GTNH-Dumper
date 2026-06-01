package com.iouter.gtnhdumper.common.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Objects;

public class AspectListSerializer implements JsonSerializer<AspectList> {
    @Override
    public JsonElement serialize(AspectList src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return null;
        }
        JsonObject obj = new JsonObject();
        LinkedHashMap<Aspect, Integer> aspects = src.aspects;
        if (aspects == null || aspects.isEmpty() || aspects.keySet().stream().allMatch(Objects::isNull)) {
            return null;
        }
        for (Aspect aspect : aspects.keySet()) {
            if (aspect == null) {
                continue;
            }
            obj.addProperty(aspect.getName(), aspects.get(aspect));
        }
        return obj;
    }
}
