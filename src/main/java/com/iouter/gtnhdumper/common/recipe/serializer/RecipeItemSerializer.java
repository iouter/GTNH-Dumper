package com.iouter.gtnhdumper.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.iouter.gtnhdumper.common.recipe.base.RecipeItem;

import java.lang.reflect.Type;

public class RecipeItemSerializer implements JsonSerializer<RecipeItem> {
    @Override
    public JsonElement serialize(RecipeItem src, Type typeOfSrc, JsonSerializationContext context) {
        boolean hasOnlyKey = src.amount == null && src.chance == null && src.nbt == null;

        if (hasOnlyKey) {
            if (src.key != null && src.keys == null)
                return new JsonPrimitive(src.key);
            if (src.key == null && src.keys != null)
                return context.serialize(src.keys);
            if (src.key == null)
                return new JsonPrimitive("");
        }

        JsonObject obj = new JsonObject();
        if (src.key != null) {
            obj.addProperty("key", src.key);
        }else if (src.keys != null) {
            obj.add("key", context.serialize(src.keys));
        }
        if (src.amount != null) {
            if (src.amount == 0)
                obj.addProperty("nc", true);
            else
                obj.addProperty("amount", src.amount);
        }
        if (src.chance != null) {
            obj.addProperty("chance", src.chance);
        }
        if (src.nbt != null) {
            obj.addProperty("nbt", src.nbt);
        }

        return obj;
    }
}
