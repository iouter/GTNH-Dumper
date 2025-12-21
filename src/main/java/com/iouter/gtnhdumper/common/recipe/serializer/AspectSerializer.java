package com.iouter.gtnhdumper.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import thaumcraft.api.aspects.Aspect;

import java.lang.reflect.Type;

public class AspectSerializer implements JsonSerializer<Aspect> {
    @Override
    public JsonElement serialize(Aspect src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getName());
    }
}
