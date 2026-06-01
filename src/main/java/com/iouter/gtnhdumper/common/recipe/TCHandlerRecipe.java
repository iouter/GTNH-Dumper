package com.iouter.gtnhdumper.common.recipe;

import codechicken.nei.NEIServerUtils;
import codechicken.nei.recipe.IRecipeHandler;
import com.gtnewhorizons.aspectrecipeindex.nei.AlchemyRecipeHandler;
import com.gtnewhorizons.aspectrecipeindex.nei.AspectCombinationHandler;
import com.gtnewhorizons.aspectrecipeindex.nei.InfusionRecipeHandler;
import com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench.ShapedArcaneRecipeHandler;
import com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench.ShapelessArcaneRecipeHandler;
import com.iouter.gtnhdumper.common.recipe.base.BaseHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.base.RecipeItem;
import com.iouter.gtnhdumper.common.utils.RecipeUtil;
import net.glease.tc4tweak.api.infusionrecipe.EnhancedInfusionRecipe;
import net.glease.tc4tweak.api.infusionrecipe.InfusionRecipeExt;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.crafting.ShapelessArcaneRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TCHandlerRecipe extends BaseHandlerRecipe {
    public TCHandlerRecipe(IRecipeHandler handler) {
        super(handler);
    }

    @Override
    public List<?> getRecipes(IRecipeHandler handler) {
        List<TCRecipe> recipes = new ArrayList<>();
        if (handler instanceof AspectCombinationHandler) {
            for (Aspect aspect : Aspect.getCompoundAspects()) {
                Aspect[] components = aspect.getComponents();
                if (components == null)
                    continue;
                AspectList inputAspects = new AspectList();
                for (Aspect iA : components) {
                    inputAspects.add(iA, 1);
                }
                AspectList outputAspects = new AspectList().add(aspect, 1);
                recipes.add(new TCRecipe().withInputAspects(inputAspects).withOutputAspects(outputAspects));
            }
        } else if (handler instanceof ShapedArcaneRecipeHandler) {
            for (Object o : ThaumcraftApi.getCraftingRecipes()) {
                if (!(o instanceof ShapedArcaneRecipe)) {
                    continue;
                }
                ShapedArcaneRecipe tcRecipe = (ShapedArcaneRecipe) o;
                Object[] inputItems = new Object[9];
                for (int i = 0; i < tcRecipe.getInput().length; i++) {
                    Object oInput = tcRecipe.getInput()[i];
                    if (isItem(oInput)) {
                        inputItems[i] = getItemFromObject(oInput);
                    }
                }
                recipes.add(new TCRecipe()
                    .withInputItems(inputItems)
                    .withInputAspects(tcRecipe.aspects)
                    .withOutputItems(new RecipeItem(tcRecipe.getRecipeOutput()))
                    .withResearch(tcRecipe.getResearch())
                );
            }
        } else if (handler instanceof ShapelessArcaneRecipeHandler) {
            for (Object o : ThaumcraftApi.getCraftingRecipes()) {
                if (o instanceof ShapelessArcaneRecipe) {
                    ShapelessArcaneRecipe tcRecipe = (ShapelessArcaneRecipe) o;
                    recipes.add(new TCRecipe().withInputItems(Arrays
                            .stream(tcRecipe.getInput().toArray())
                            .filter(TCHandlerRecipe::isItem)
                            .map(TCHandlerRecipe::getItemFromObject)
                            .toArray(Object[]::new))
                        .withInputAspects(tcRecipe.getAspects())
                        .withOutputItems(new RecipeItem(tcRecipe.getRecipeOutput()))
                        .withResearch(tcRecipe.getResearch()));
                }
            }
        } else if (handler instanceof AlchemyRecipeHandler) {
            for (Object o : ThaumcraftApi.getCraftingRecipes()) {
                if (!(o instanceof CrucibleRecipe)) {
                    continue;
                }
                CrucibleRecipe tcRecipe = (CrucibleRecipe) o;
                recipes.add(new TCRecipe()
                    .withInputItems(getItemFromObject(tcRecipe.catalyst))
                    .withInputAspects(tcRecipe.aspects)
                    .withOutputItems(new RecipeItem(tcRecipe.getRecipeOutput()))
                    .withResearch(tcRecipe.key)
                );
            }
        } else if (handler instanceof InfusionRecipeHandler) {
            for (Object o : ThaumcraftApi.getCraftingRecipes()) {
                if (!(o instanceof InfusionRecipe)) {
                    continue;
                }
                InfusionRecipe tcRecipe = (InfusionRecipe) o;
                try {
                    EnhancedInfusionRecipe r = InfusionRecipeExt.get().convert(tcRecipe);
                    ItemStack outputStack;
                    if (tcRecipe.getRecipeOutput() instanceof ItemStack) {
                        outputStack = (ItemStack) tcRecipe.getRecipeOutput();
                    } else {
                        outputStack = tcRecipe.getRecipeInput().copy();
                        Object[] obj = (Object[]) tcRecipe.getRecipeOutput();
                        NBTBase tag = (NBTBase) obj[1];
                        outputStack.setTagInfo((String) obj[0], tag);
                    }
                    recipes.add(new TCRecipe()
                        .withKeyItem(getItemFromObject(r.getCentral().getRepresentativeStacks()))
                        .withInputItems(r
                            .getComponentsExt()
                            .stream()
                            .map(rI -> getItemFromObject(rI.getRepresentativeStacks()))
                            .filter(java.util.Objects::nonNull)
                            .toArray(Object[]::new))
                        .withInputAspects(tcRecipe.getAspects())
                        .withOutputItems(getItemFromObject(outputStack))
                        .withInstability(tcRecipe.getInstability())
                        .withResearch(tcRecipe.getResearch())
                    );
                } catch (Exception ignored) {
                }
            }
        }
        if (recipes.isEmpty()) {
            return null;
        }
        return recipes;
    }

    public static boolean isItem(Object o) {
        return (o instanceof ItemStack || o instanceof ItemStack[]
            || o instanceof String
            || (o instanceof List && !((List<?>) o).isEmpty()));
    }

    public static Object getItemFromObject(Object o) {
        ItemStack[] stacks = NEIServerUtils.extractRecipeItems(o);
        return RecipeUtil.getRecipeItems(stacks);
    }

    public static class TCRecipe {
        private Object keyItem;

        private Object[] inputItems;
        private AspectList inputAspects;

        private Object[] outputItems;
        private AspectList outputAspects;

        private String research;

        private Integer instability;

        public TCRecipe() {}

        public TCRecipe withKeyItem(Object keyItem) {
            this.keyItem = keyItem;
            return this;
        }

        public TCRecipe withInputItems(Object... recipeItems) {
            this.inputItems = recipeItems;
            return this;
        }

        public TCRecipe withInputAspects(AspectList aspects) {
            this.inputAspects = aspects;
            return this;
        }

        public TCRecipe withOutputItems(Object... recipeItems) {
            this.outputItems = recipeItems;
            return this;
        }

        public TCRecipe withOutputAspects(AspectList aspects) {
            this.outputAspects = aspects;
            return this;
        }

        public TCRecipe withResearch(String research) {
            this.research = research;
            return this;
        }

        public TCRecipe withInstability(int instability) {
            this.instability = instability;
            return this;
        }
    }
}
