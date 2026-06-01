package com.iouter.gtnhdumper.common.recipe;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.iouter.gtnhdumper.common.recipe.base.BaseHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.base.BaseRecipe;
import com.iouter.gtnhdumper.common.utils.RecipeUtil;
import com.iouter.gtnhdumper.common.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShapedCraftingHandlerRecipe extends BaseHandlerRecipe {
    public ShapedCraftingHandlerRecipe(IRecipeHandler handler) {
        super(handler);
    }

    @Override
    public List<?> getRecipes(IRecipeHandler handler) {
        List<BaseRecipe> recipes = new ArrayList<>();
        if (!(handler instanceof TemplateRecipeHandler)) {
            return null;
        }
        TemplateRecipeHandler recipeHandler = (TemplateRecipeHandler) handler;
        recipeHandler.loadCraftingRecipes(recipeHandler.getOverlayIdentifier(), (Object) null);
        for (TemplateRecipeHandler.CachedRecipe recipe : recipeHandler.arecipes) {
            Object[] inputItems = new Object[9];
            for (PositionedStack p : recipe.getIngredients()) {
                int serial = Utils.getCraftingSerial(p) - 1;
                if (serial == -1)
                    continue;
                inputItems[serial] = RecipeUtil.getRecipeItems(p.items);
            }
            ArrayList<Object> otherItems = Utils.getRecipeItems(recipe.getOtherStacks());
            if (otherItems.isEmpty())
                otherItems = null;
            BaseRecipe baseRecipe = new BaseRecipe(new ArrayList<>(Arrays.asList(inputItems)),
                null,
                Utils.getRecipeItems(recipe.getResult()),
                null,
                otherItems);
            recipes.add(baseRecipe);
        }
        return recipes;
    }
}
