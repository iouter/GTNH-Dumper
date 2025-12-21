package com.iouter.gtnhdumper.common.recipe.base;

import thaumcraft.api.aspects.AspectList;

public class TCRecipe {
    private RecipeItem keyItem;

    private RecipeItem[] inputItems;
    private AspectList inputAspects;

    private RecipeItem[] outputItems;
    private AspectList outputAspects;

    private String research;

    private Integer instability;

    public TCRecipe() {}

    public TCRecipe withKeyItem(RecipeItem keyItem) {
        this.keyItem = keyItem;
        return this;
    }

    public TCRecipe withInputItems(RecipeItem... recipeItems) {
        this.inputItems = recipeItems;
        return this;
    }

    public TCRecipe withInputAspects(AspectList aspects) {
        this.inputAspects = aspects;
        return this;
    }

    public TCRecipe withOutputItems(RecipeItem... recipeItems) {
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
