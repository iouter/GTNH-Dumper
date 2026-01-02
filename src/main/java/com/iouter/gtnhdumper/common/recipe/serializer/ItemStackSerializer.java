package com.iouter.gtnhdumper.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.iouter.gtnhdumper.common.recipe.base.RecipeItem;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Type;

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
