package com.iouter.gtnhdumper.common.recipe.base;

import gregtech.api.recipe.metadata.IRecipeMetadataStorage;

import java.util.ArrayList;
import java.util.Map;

public class GTRecipe{

    private final ArrayList<Object> inputItems;
    private final ArrayList<RecipeFluid> inputFluids;
    private final ArrayList<Object> outputItems;
    private final ArrayList<RecipeFluid> outputFluids;
    private final ArrayList<Object> otherItems;
    private final Integer eut;
    private final Integer duration;
    private Integer specialValue;
    private final Map<String, Object> metadata;

    public GTRecipe(ArrayList<Object> inputItems,
                    ArrayList<RecipeFluid> inputFluids,
                    ArrayList<Object> outputItems,
                    ArrayList<RecipeFluid> outputFluids,
                    ArrayList<Object> otherItems, int eut, int duration, Integer specialValue, Map<String, Object> metadata) {
        this.inputItems = inputItems;
        this.inputFluids = inputFluids;
        this.outputItems = outputItems;
        this.outputFluids = outputFluids;
        this.otherItems = otherItems;
        this.eut = eut;
        this.duration = duration;
        this.metadata = metadata;
        if (specialValue != 0)
            this.specialValue = specialValue;
    }
}
