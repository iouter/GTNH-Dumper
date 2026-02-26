package com.iouter.gtnhdumper.common.recipe.base;

import java.util.ArrayList;
import java.util.Collection;

public class ForestryRecipe {
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
