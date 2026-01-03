package com.iouter.gtnhdumper.common.recipe;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiRecipeTab;
import codechicken.nei.recipe.HandlerInfo;
import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.RecipeCatalysts;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.google.common.base.Objects;
import com.iouter.gtnhdumper.Utils;
import com.iouter.gtnhdumper.common.recipe.base.BaseRecipe;
import com.iouter.gtnhdumper.common.recipe.base.RecipeItem;
import com.iouter.gtnhdumper.common.recipe.base.RecipeUtil;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;

public class ShapedCraftingHandlerRecipe{
    private final String name;
    private final String identifier;
    private final String source;
    private final String markedItem;
    private final ArrayList<String> catalysts;
    private final ArrayList<BaseRecipe> recipes;

    public ShapedCraftingHandlerRecipe(IRecipeHandler handler) {
        this.name = handler.getRecipeName();
        this.catalysts = new ArrayList<>();

        final String handlerName = handler.getHandlerId();
        final String handlerId = Objects.firstNonNull(
            handler instanceof TemplateRecipeHandler ? ((TemplateRecipeHandler) handler).getOverlayIdentifier()
                : null,
            "null");

        this.identifier = handlerId;
        this.source = handlerName;

        HandlerInfo info = GuiRecipeTab.getHandlerInfo(handlerName, handlerId);
        final ItemStack markedItemStack = info != null ? info.getItemStack() : null;
        this.markedItem = markedItemStack != null ? Utils.getItemKeyWithNBT(markedItemStack) : "null";

        RecipeCatalysts.getRecipeCatalysts(handler).stream().forEach(positionedStack -> {
            ItemStack[] items = positionedStack.items;
            if (items == null)
                return;
            for (ItemStack stack: items) {
                catalysts.add(Utils.getItemKeyWithNBT(stack));
            }
        });

        this.recipes = new ArrayList<>();
        if (handler instanceof TemplateRecipeHandler) {
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
        }
    }
}
