package com.iouter.gtnhdumper.common.recipe.base;

import com.iouter.gtnhdumper.Utils;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.Map;

public class RecipeItem {
    public static final Map<String, String[]> oreDictMap = Utils.getOreDict();

    public String key;
    public String[] keys;
    public final Integer amount;
    public Integer chance;
    public String nbt;


    public RecipeItem(String key, int amount) {
        this.key = key;
        if (amount != 1) {
            this.amount = amount;
        } else {
            this.amount = null;
        }
    }

    public RecipeItem(ItemStack stack) {
        if (stack != null) {
            this.key = Utils.getItemKey(stack);
            int amount = stack.stackSize;
            if (amount != 1) {
                this.amount = amount;
            } else {
                this.amount = null;
            }
        } else {
            this.key = null;
            this.amount = null;
        }
        this.withNBT(stack);
    }

    public RecipeItem(ItemStack[] stacks) {
        final ItemStack stack = stacks[0];
        if (stacks.length == 1) {
            this.key = Utils.getItemKey(stack);
        } else {
            final String oD = Utils.getOreDictByItems(stacks, oreDictMap);
            if (oD != null) {
                this.key = "#" + oD;
            } else {
                this.keys = Arrays.stream(stacks).map(Utils::getItemKey).toArray(String[]::new);
                this.withNBT(stack);
            }
        }
        int amount = stack.stackSize;
        if (amount != 1) {
            this.amount = amount;
        } else {
            this.amount = null;
        }
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
}
