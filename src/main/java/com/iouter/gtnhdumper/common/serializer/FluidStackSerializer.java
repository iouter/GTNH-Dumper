package com.iouter.gtnhdumper.common.serializer;

import java.lang.reflect.Type;

import net.minecraftforge.fluids.FluidStack;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.iouter.gtnhdumper.common.recipe.base.RecipeFluid;

public class FluidStackSerializer implements JsonSerializer<FluidStack> {

    @Override
    public JsonElement serialize(FluidStack src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) return null;
        RecipeFluid recipeFluid = new RecipeFluid(src).withNBT(src);
        return context.serialize(recipeFluid, RecipeFluid.class);
    }
}
