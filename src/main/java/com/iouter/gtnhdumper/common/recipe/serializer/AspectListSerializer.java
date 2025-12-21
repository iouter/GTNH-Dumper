package com.iouter.gtnhdumper.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;

public class AspectListSerializer implements JsonSerializer<AspectList> {
    @Override
    public JsonElement serialize(AspectList src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        LinkedHashMap<Aspect, Integer> aspects = src.aspects;
        for (Aspect aspect : aspects.keySet()) {
            obj.addProperty(aspect.getName(), aspects.get(aspect));
        }
        return obj;
    }
}
