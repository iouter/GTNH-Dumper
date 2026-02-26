package com.iouter.gtnhdumper.common.recipe.utils;

import codechicken.nei.PositionedStack;
import com.iouter.gtnhdumper.Utils;
import com.iouter.gtnhdumper.common.recipe.base.RecipeItem;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.iouter.gtnhdumper.common.recipe.base.RecipeItem.oreDictMap;

public class RecipeUtil {
    private RecipeUtil() {}

    public static Object getRecipeItems(ItemStack[] stacks) {
        if (stacks == null || stacks.length == 0) {
            return null;
        }
        stacks = Arrays.stream(stacks).filter(Objects::nonNull).toArray(ItemStack[]::new);
        if (stacks.length == 1) {
            return new RecipeItem(stacks[0]);
        }
        final ItemStack stack0 = stacks[0];
        final int amount = stack0.stackSize;
        final String nbt = Utils.getItemNBT(stack0);
        final boolean isDataEqual = Arrays.stream(stacks).allMatch(stack -> {
            final boolean isAmountEqual = amount == stack.stackSize;
            if (!isAmountEqual) {
                return false;
            }
            if (nbt == null) {
                if (Utils.getItemNBT(stack) == null) {
                    return isAmountEqual;
                }
                return false;
            }
            return nbt.equals(Utils.getItemNBT(stack));
        });
        if (isDataEqual) {
            final String oD = Utils.getOreDictByItems(stacks, oreDictMap);
            if (oD != null) {
                return new RecipeItem("#" + oD, amount).withNBT(nbt);
            }
        }
        return Arrays.stream(stacks).map(RecipeItem::new).toArray(RecipeItem[]::new);
    }

    public static Object getRecipeItems(PositionedStack stacks) {
        return getRecipeItems(stacks.items);
    }

    public static ArrayList<Object> getRecipeItemList(ArrayList<PositionedStack> stacks) {
        return stacks.stream().map(RecipeUtil::getRecipeItems).collect(Collectors.toCollection(ArrayList::new));
    }

    public static ArrayList<Object> getRecipeItemList(PositionedStack stacks) {
        ArrayList<PositionedStack> list = new ArrayList<>();
        list.add(stacks);
        return getRecipeItemList(list);
    }
}
