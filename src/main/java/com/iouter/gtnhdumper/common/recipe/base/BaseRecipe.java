package com.iouter.gtnhdumper.common.recipe.base;

import java.util.ArrayList;

public class BaseRecipe {
    private final ArrayList<RecipeItem> inputItems;
    private final ArrayList<RecipeFluid> inputFluids;
    private final ArrayList<RecipeItem> outputItems;
    private final ArrayList<RecipeFluid> outputFluids;
    private final ArrayList<RecipeItem> otherItems;

    public BaseRecipe(ArrayList<RecipeItem> inputItems, ArrayList<RecipeFluid> inputFluids, ArrayList<RecipeItem> outputItems, ArrayList<RecipeFluid> outputFluids, ArrayList<RecipeItem> otherItems) {
        this.inputItems = inputItems;
        this.inputFluids = inputFluids;
        this.outputItems = outputItems;
        this.outputFluids = outputFluids;
        this.otherItems = otherItems;
    }
}
