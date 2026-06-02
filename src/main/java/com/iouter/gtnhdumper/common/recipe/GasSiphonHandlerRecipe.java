package com.iouter.gtnhdumper.common.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraftforge.fluids.FluidStack;

import com.iouter.gtnhdumper.common.recipe.base.BaseHandlerRecipe;

import codechicken.nei.recipe.IRecipeHandler;
import gtnhintergalactic.recipe.GasSiphonRecipes;
import lombok.Setter;
import lombok.experimental.Accessors;

public class GasSiphonHandlerRecipe extends BaseHandlerRecipe {

    public GasSiphonHandlerRecipe(IRecipeHandler handler) {
        super(handler);
    }

    @Override
    public List<?> getRecipes(IRecipeHandler baseHandler) {
        List<GasSiphonRecipe> recipes = new ArrayList<>();
        for (Map.Entry<String, Map<Integer, FluidStack>> entry : GasSiphonRecipes.RECIPES.entrySet()) {
            for (Map.Entry<Integer, FluidStack> innerEntry : entry.getValue()
                .entrySet()) {
                recipes.add(
                    new GasSiphonRecipe().setFluid(innerEntry.getValue())
                        .setPlanet(entry.getKey())
                        .setDepth(innerEntry.getKey())
                        .setOutputAmount(innerEntry.getValue().amount));
            }
        }
        return recipes;
    }

    @Setter
    @Accessors(chain = true)
    private static class GasSiphonRecipe {

        private FluidStack fluid;
        private String planet;
        private int depth;
        private int outputAmount;
    }
}
