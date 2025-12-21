package com.iouter.gtnhdumper.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import gregtech.api.enums.Element;

import java.lang.reflect.Type;

public class ElementSerializer implements JsonSerializer<Element> {
    @Override
    public JsonElement serialize(Element src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null || src == Element._NULL) {
            return new JsonPrimitive("Empty");
        }
        JsonObject obj = new JsonObject();

        obj.addProperty("name", src.name());
        if (src.mName != null)
            obj.addProperty("fullName", src.mName);
        obj.addProperty("protons", src.getProtons());
        obj.addProperty("neutrons", src.getNeutrons());
        obj.addProperty("mass", src.getMass());
        return obj;
    }
}
