package com.iouter.gtnhdumper.common.recipe;

import codechicken.nei.recipe.IRecipeHandler;
import com.iouter.gtnhdumper.common.recipe.base.BaseHandlerRecipe;
import gtnhintergalactic.recipe.SpacePumpingRecipes;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpacePumpModuleHandlerRecipe extends BaseHandlerRecipe {

    public SpacePumpModuleHandlerRecipe(IRecipeHandler handler) {
        super(handler);
    }

    @Override
    public List<?> getRecipes(IRecipeHandler handler) {
        List<SpacePumpModuleRecipe> recipes = new ArrayList<>();
        for (Map.Entry<Pair<Integer, Integer>, FluidStack> entry : SpacePumpingRecipes.RECIPES.entrySet()) {
            recipes.add(
                new SpacePumpModuleRecipe().setFluid(entry.getValue())
                    .setPlanetType(
                        entry.getKey()
                            .getLeft())
                    .setGasType(
                        entry.getKey()
                            .getRight())
                    .setAmount(entry.getValue().amount));
        }
        return recipes;
    }

    @Setter
    @Accessors(chain = true)
    private static class SpacePumpModuleRecipe {

        private FluidStack fluid;
        private int planetType;
        private int gasType;
        private int amount;
    }
}
