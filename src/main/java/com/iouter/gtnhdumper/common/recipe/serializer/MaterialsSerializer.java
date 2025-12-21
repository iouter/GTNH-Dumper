package com.iouter.gtnhdumper.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import gregtech.api.enums.Materials;

import java.lang.reflect.Type;

public class MaterialsSerializer implements JsonSerializer<Materials> {

    @Override
    public JsonElement serialize(Materials src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }
}
