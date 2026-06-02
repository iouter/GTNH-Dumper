package com.iouter.gtnhdumper.common.serializer;

import java.lang.reflect.Type;

import net.minecraft.item.ItemStack;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.iouter.gtnhdumper.common.recipe.base.RecipeItem;

public class ItemStackSerializer implements JsonSerializer<ItemStack> {

    @Override
    public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return null;
        }
        RecipeItem recipeItem = new RecipeItem(src);
        return context.serialize(recipeItem, RecipeItem.class);
    }
}
