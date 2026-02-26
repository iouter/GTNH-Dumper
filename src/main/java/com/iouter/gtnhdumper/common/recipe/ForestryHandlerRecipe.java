package com.iouter.gtnhdumper.common.recipe;

import codechicken.nei.recipe.GuiRecipeTab;
import codechicken.nei.recipe.HandlerInfo;
import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.RecipeCatalysts;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.google.common.base.Objects;
import com.iouter.gtnhdumper.Utils;
import com.iouter.gtnhdumper.common.recipe.base.ForestryRecipe;
import com.iouter.gtnhdumper.common.recipe.base.RecipeItem;
import com.iouter.gtnhdumper.common.recipe.utils.RecipeUtil;
import forestry.api.apiculture.IAlleleBeeSpeciesCustom;
import forestry.api.apiculture.IJubilanceProvider;
import forestry.api.genetics.IAlleleSpecies;
import net.bdew.neiaddons.forestry.AddonForestry;
import net.bdew.neiaddons.forestry.BaseBreedingRecipeHandler;
import net.bdew.neiaddons.forestry.BaseProduceRecipeHandler;
import net.bdew.neiaddons.forestry.GeneticsUtils;
import net.bdew.neiaddons.utils.LabeledPositionedStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ForestryHandlerRecipe {
    private final String name;
    private final String identifier;
    private final String source;
    private final String markedItem;
    private final ArrayList<String> catalysts;
    private final ArrayList<ForestryRecipe> recipes;

    public ForestryHandlerRecipe(IRecipeHandler handler) {
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
            for (ItemStack stack : items) {
                catalysts.add(Utils.getItemKeyWithNBT(stack));
            }
        });
        this.recipes = new ArrayList<>();
        if (handler instanceof BaseBreedingRecipeHandler) {
            BaseBreedingRecipeHandler breeding = (BaseBreedingRecipeHandler) handler;
            try {
                breeding.loadCraftingRecipes(breeding.getRecipeIdent(), (Object) null);
            } catch (Exception ignored) {

            }
            breeding.arecipes.stream().filter(BaseBreedingRecipeHandler.CachedBreedingRecipe.class::isInstance)
                .map(BaseBreedingRecipeHandler.CachedBreedingRecipe.class::cast).forEachOrdered(recipe ->
                    recipes.add(new ForestryRecipe()
                        .setInputItems(RecipeUtil.getRecipeItemList(recipe.getIngredients()))
                        .setOutputItems(RecipeUtil.getRecipeItemList(recipe.getResult()))
                        .setChance(recipe.chance * 100)
                        .setRequirements(recipe.requirements)
                    )
                );
        } else if (handler instanceof BaseProduceRecipeHandler) {
            BaseProduceRecipeHandler produce = (BaseProduceRecipeHandler) handler;
            for (IAlleleSpecies species : produce.getAllSpecies()) {
                ForestryRecipe recipe = new ForestryRecipe();
                ItemStack inputStack = GeneticsUtils.stackFromSpecies(species, GeneticsUtils.RecipePosition.Producer);
                recipe.setInputItems(new RecipeItem(inputStack));
                for (Map.Entry<ItemStack, Float> product : net.bdew.neiaddons.Utils.mergeStacks(GeneticsUtils.getProduceFromSpecies(species))
                    .entrySet()) {
                    recipe.setOutputItems(new RecipeItem(product.getKey()).withChance((int) (product.getValue() * 10000)));
                }
                String jubilance = null;
                if (species instanceof IAlleleBeeSpeciesCustom) {
                    IJubilanceProvider provider = ((IAlleleBeeSpeciesCustom) species).getJubilanceProvider();
                    if (provider != null) jubilance = provider.getDescription();
                }
                for (Map.Entry<ItemStack, Float> product : net.bdew.neiaddons.Utils.mergeStacks(GeneticsUtils.getSpecialtyFromSpecies(species))
                    .entrySet()) {
                    RecipeItem item = new RecipeItem(product.getKey()).withChance((int) (product.getValue() * 10000));
                    if (jubilance != null) {
                        recipe.setOtherItems(item.withTooltip(jubilance));
                    } else {
                        recipe.setOtherItems(item);
                    }
                }
                recipes.add(recipe);
            }
        }
    }

    public static Object getRecipeItems(LabeledPositionedStack stacks) {
        Object o = RecipeUtil.getRecipeItems(stacks.items);
        if (o instanceof RecipeItem) {
            return ((RecipeItem) o).withTooltip(stacks.getTooltip());
        }
        return o;
    }

    public static ArrayList<Object> getRecipeItemList(ArrayList<LabeledPositionedStack> stacks) {
        if (stacks == null) {
            return null;
        }
        return stacks.stream().map(ForestryHandlerRecipe::getRecipeItems).collect(Collectors.toCollection(ArrayList::new));
    }
}
