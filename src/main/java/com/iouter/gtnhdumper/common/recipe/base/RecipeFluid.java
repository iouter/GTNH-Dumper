package com.iouter.gtnhdumper.common.recipe.base;

import com.iouter.gtnhdumper.Utils;
import net.minecraftforge.fluids.FluidStack;

public class RecipeFluid {
    private final String key;
    private long amount;
    private Integer chance;
    public String nbt;


    public RecipeFluid(String key, int amount) {
        this.key = key;
        this.amount = amount;
    }

    public RecipeFluid(FluidStack stack) {
        this("fluid." + stack.getFluid().getName(), stack.amount);
    }

    public RecipeFluid withChance(int chance) {
        this.chance = chance;
        return this;
    }

    public RecipeFluid withNBT(String nbt) {
        this.nbt = nbt;
        return this;
    }

    public RecipeFluid withNBT(FluidStack stack) {
        if (stack == null)
            return this;
        String nbt = Utils.getFluidNBT(stack);
        if (nbt != null) {
            return withNBT(nbt);
        }
        return this;
    }

    public RecipeFluid withAmount(long amount) {
        this.amount = amount;
        return this;
    }
}
