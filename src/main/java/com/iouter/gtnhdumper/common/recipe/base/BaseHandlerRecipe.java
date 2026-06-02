package com.iouter.gtnhdumper.common.recipe.base;

import codechicken.nei.recipe.GuiRecipeTab;
import codechicken.nei.recipe.HandlerInfo;
import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.RecipeCatalysts;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.google.common.base.Objects;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.iouter.gtnhdumper.common.utils.RecipeUtil;
import net.minecraft.item.ItemStack;

import java.util.List;

import static com.iouter.gtnhdumper.GTNHDumper.GSON;

public abstract class BaseHandlerRecipe {

    private final JsonObject json = new JsonObject();

    public BaseHandlerRecipe(IRecipeHandler handler) {
        json.addProperty("name", handler.getRecipeName());
        JsonArray catalysts = new JsonArray();

        RecipeCatalysts.getRecipeCatalysts(handler)
            .forEach(p -> catalysts.add(GSON.toJsonTree(RecipeUtil.getRecipeItems(p))));

        json.add("catalysts", catalysts);

        final String handlerName = handler.getHandlerId();
        final String handlerId = Objects
            .firstNonNull(handler instanceof TemplateRecipeHandler ? handler.getOverlayIdentifier() : null, "null");

        json.addProperty("identifier", handlerId);
        json.addProperty("source", handlerName);
        HandlerInfo info = GuiRecipeTab.getHandlerInfo(handlerName, handlerId);
        final ItemStack markedItemStack = info != null ? info.getItemStack() : null;
        json.add("markedItem", markedItemStack != null ? GSON.toJsonTree(markedItemStack) : GSON.toJsonTree("null"));

        addJsonObject(json, handler);

        json.add("recipes", GSON.toJsonTree(getRecipes(handler)));
    }

    public void addJsonObject(JsonObject json, IRecipeHandler handler) {

    }

    public abstract List<?> getRecipes(IRecipeHandler handler);

    public JsonObject build() {
        return json;
    }
}
