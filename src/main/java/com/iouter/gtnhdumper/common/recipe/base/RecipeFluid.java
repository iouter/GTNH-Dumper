package com.iouter.gtnhdumper.common.recipe.base;

import net.minecraftforge.fluids.FluidStack;

import com.iouter.gtnhdumper.common.utils.Utils;

public class RecipeFluid {

    private final String key;
    private Long amount;
    private Integer chance;
    public String nbt;

    public RecipeFluid(String key, long amount) {
        this.key = key;
        if (amount != 0) {
            this.amount = amount;
        } else {
            this.amount = null;
        }
    }

    public RecipeFluid(FluidStack stack) {
        this(
            "fluid." + stack.getFluid()
                .getName(),
            (long) stack.amount);
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
        if (stack == null) return this;
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
