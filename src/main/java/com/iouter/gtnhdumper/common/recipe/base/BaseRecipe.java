package com.iouter.gtnhdumper.common.recipe.base;

import java.util.ArrayList;

public class BaseRecipe {
    private final ArrayList<Object> inputItems;
    private final ArrayList<RecipeFluid> inputFluids;
    private final ArrayList<Object> outputItems;
    private final ArrayList<RecipeFluid> outputFluids;
    private final ArrayList<Object> otherItems;

    public BaseRecipe(ArrayList<Object> inputItems,
                      ArrayList<RecipeFluid> inputFluids,
                      ArrayList<Object> outputItems,
                      ArrayList<RecipeFluid> outputFluids,
                      ArrayList<Object> otherItems) {
        this.inputItems = inputItems;
        this.inputFluids = inputFluids;
        this.outputItems = outputItems;
        this.outputFluids = outputFluids;
        this.otherItems = otherItems;
    }
}
