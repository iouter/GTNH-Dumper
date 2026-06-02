package com.iouter.gtnhdumper.common.recipe;

import java.util.ArrayList;
import java.util.List;

import com.iouter.gtnhdumper.common.recipe.base.BaseHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.base.BaseRecipe;
import com.iouter.gtnhdumper.common.utils.Utils;

import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class GeneralHandlerRecipe extends BaseHandlerRecipe {

    public GeneralHandlerRecipe(IRecipeHandler handler) {
        super(handler);
    }

    @Override
    public List<?> getRecipes(IRecipeHandler handler) {
        List<BaseRecipe> recipes = new ArrayList<>();
        if (!(handler instanceof TemplateRecipeHandler recipeHandler)) {
            return null;
        }
        try {
            recipeHandler.loadCraftingRecipes(recipeHandler.getOverlayIdentifier(), (Object) null);
        } catch (Exception ignored) {
            return null;
        }
        for (TemplateRecipeHandler.CachedRecipe recipe : recipeHandler.arecipes) {
            ArrayList<Object> otherItems;
            try {
                otherItems = Utils.getRecipeItems(recipe.getOtherStacks());
                if (otherItems.isEmpty()) otherItems = null;
            } catch (Exception e) {
                otherItems = null;
            }
            BaseRecipe baseRecipe = new BaseRecipe(
                Utils.getRecipeItems(recipe.getIngredients()),
                null,
                Utils.getRecipeItems(recipe.getResult()),
                null,
                otherItems);
            recipes.add(baseRecipe);
        }
        return recipes;
    }
}
