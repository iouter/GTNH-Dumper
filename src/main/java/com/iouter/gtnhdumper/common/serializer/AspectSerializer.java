package com.iouter.gtnhdumper.common.serializer;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import thaumcraft.api.aspects.Aspect;

public class AspectSerializer implements JsonSerializer<Aspect> {

    @Override
    public JsonElement serialize(Aspect src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return null;
        }
        return new JsonPrimitive(src.getName());
    }
}
