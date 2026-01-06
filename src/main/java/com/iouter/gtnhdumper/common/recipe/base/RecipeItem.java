package com.iouter.gtnhdumper.common.recipe.base;

import com.iouter.gtnhdumper.Utils;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.Map;

public class RecipeItem {
    public static final Map<String, String[]> oreDictMap = Utils.getOreDict();

    public String key;
    public Long amount;
    public Integer chance;
    public String nbt;


    public RecipeItem(String key, int amount) {
        this.key = key;
        if (amount != 1) {
            this.amount = (long) amount;
        } else {
            this.amount = null;
        }
    }

    public RecipeItem(ItemStack stack) {
        if (stack != null) {
            this.key = Utils.getItemKey(stack);
            int amount = stack.stackSize;
            if (amount != 1) {
                this.amount = (long) amount;
            } else {
                this.amount = null;
            }
        } else {
            this.key = null;
            this.amount = null;
        }
        this.withNBT(stack);
    }

    public RecipeItem withChance(int chance) {
        if (chance != 10000) {
            this.chance = chance;
        }
        return this;

    }

    public RecipeItem withNBT(String nbt) {
        this.nbt = nbt;
        return this;
    }

    public RecipeItem withNBT(ItemStack stack) {
        if (stack == null)
            return this;
        String nbt = Utils.getItemNBT(stack);
        if (nbt != null) {
            return withNBT(nbt);
        }
        return this;
    }

    public RecipeItem withAmount(long amount) {
        this.amount = amount;
        return this;
    }
}
