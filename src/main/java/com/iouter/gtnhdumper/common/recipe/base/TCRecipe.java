package com.iouter.gtnhdumper.common.recipe.base;

import thaumcraft.api.aspects.AspectList;

public class TCRecipe {
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
