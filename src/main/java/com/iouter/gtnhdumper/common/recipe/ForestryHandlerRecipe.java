package com.iouter.gtnhdumper.common.recipe;

import codechicken.nei.recipe.IRecipeHandler;
import com.iouter.gtnhdumper.common.recipe.base.BaseHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.base.RecipeItem;
import com.iouter.gtnhdumper.common.utils.RecipeUtil;
import forestry.api.apiculture.IAlleleBeeSpeciesCustom;
import forestry.api.apiculture.IJubilanceProvider;
import forestry.api.genetics.IAlleleSpecies;
import net.bdew.neiaddons.forestry.BaseBreedingRecipeHandler;
import net.bdew.neiaddons.forestry.BaseProduceRecipeHandler;
import net.bdew.neiaddons.forestry.GeneticsUtils;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ForestryHandlerRecipe extends BaseHandlerRecipe {

    public ForestryHandlerRecipe(IRecipeHandler handler) {
        super(handler);
    }

    @Override
    public List<?> getRecipes(IRecipeHandler handler) {
        List<ForestryRecipe> recipes = new ArrayList<>();
        if (handler instanceof BaseBreedingRecipeHandler breeding) {
            try {
                breeding.loadCraftingRecipes(breeding.getRecipeIdent(), (Object) null);
            } catch (Exception ignored) {

            }
            breeding.arecipes.stream()
                .filter(BaseBreedingRecipeHandler.CachedBreedingRecipe.class::isInstance)
                .map(BaseBreedingRecipeHandler.CachedBreedingRecipe.class::cast)
                .forEachOrdered(
                    recipe -> recipes.add(
                        new ForestryRecipe().setInputItems(RecipeUtil.getRecipeItemList(recipe.getIngredients()))
                            .setOutputItems(RecipeUtil.getRecipeItemList(recipe.getResult()))
                            .setChance(recipe.chance * 100)
                            .setRequirements(recipe.requirements)));
        } else if (handler instanceof BaseProduceRecipeHandler produce) {
            for (IAlleleSpecies species : produce.getAllSpecies()) {
                ForestryRecipe recipe = new ForestryRecipe();
                ItemStack inputStack = GeneticsUtils.stackFromSpecies(species, GeneticsUtils.RecipePosition.Producer);
                recipe.setInputItems(new RecipeItem(inputStack));
                for (Map.Entry<ItemStack, Float> product : net.bdew.neiaddons.Utils
                    .mergeStacks(GeneticsUtils.getProduceFromSpecies(species))
                    .entrySet()) {
                    recipe.setOutputItems(
                        new RecipeItem(product.getKey()).withChance((int) (product.getValue() * 10000)));
                }
                String jubilance = null;
                if (species instanceof IAlleleBeeSpeciesCustom) {
                    IJubilanceProvider provider = ((IAlleleBeeSpeciesCustom) species).getJubilanceProvider();
                    if (provider != null) jubilance = provider.getDescription();
                }
                for (Map.Entry<ItemStack, Float> product : net.bdew.neiaddons.Utils
                    .mergeStacks(GeneticsUtils.getSpecialtyFromSpecies(species))
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
        return recipes;
    }

    public static class ForestryRecipe {

        private ArrayList<Object> inputItems;
        private ArrayList<Object> outputItems;
        private ArrayList<Object> otherItems;
        private Collection<String> requirements;
        private Float chance;

        public ForestryRecipe setInputItems(ArrayList<Object> inputItems) {
            this.inputItems = inputItems;
            return this;
        }

        public ForestryRecipe setOutputItems(ArrayList<Object> outputItems) {
            this.outputItems = outputItems;
            return this;
        }

        public ForestryRecipe setOtherItems(ArrayList<Object> otherItems) {
            this.otherItems = otherItems;
            return this;
        }

        public ForestryRecipe setInputItems(Object inputItems) {
            if (this.inputItems == null) {
                this.inputItems = new ArrayList<>();
            }
            this.inputItems.add(inputItems);
            return this;
        }

        public ForestryRecipe setOutputItems(Object outputItems) {
            if (this.outputItems == null) {
                this.outputItems = new ArrayList<>();
            }
            this.outputItems.add(outputItems);
            return this;
        }

        public ForestryRecipe setOtherItems(Object otherItems) {
            if (this.otherItems == null) {
                this.otherItems = new ArrayList<>();
            }
            this.otherItems.add(otherItems);
            return this;
        }

        public ForestryRecipe setChance(float chance) {
            this.chance = chance;
            return this;
        }

        public ForestryRecipe setRequirements(Collection<String> requirements) {
            this.requirements = requirements;
            return this;
        }
    }
}
