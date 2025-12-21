package com.iouter.gtnhdumper.common.recipe.utils;

import gregtech.api.objects.ItemData;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Method;

public class GTShouldCheckNBT {
    private static Method shouldCheckNBTMethod = null;
    private static Method getNoCopyMethod = null;

    static {
        try {
            Class<?> gtRecipeClass = GTRecipe.class;
            shouldCheckNBTMethod = gtRecipeClass.getDeclaredMethod("shouldCheckNBT", ItemStack.class);
            shouldCheckNBTMethod.setAccessible(true);
            Class<?> unificatorClass = GTOreDictUnificator.class;
            getNoCopyMethod = unificatorClass.getDeclaredMethod("get_nocopy", boolean.class, ItemStack.class);
            getNoCopyMethod.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean shouldCheckNBT(final ItemData oredictOther, final ItemStack other, GTRecipe gtRecipe) {
        try {
            final boolean stackNeedsNBT = (boolean) shouldCheckNBTMethod.invoke(null, other);
            final boolean usesNbtMatching = stackNeedsNBT || gtRecipe.isNBTSensitive;
            final ItemStack unifiedStack;
            if (stackNeedsNBT) {
                unifiedStack = other;
            } else {
                unifiedStack = (ItemStack) getNoCopyMethod.invoke(null, true, other);
                if (!usesNbtMatching) {
                    unifiedStack.setTagCompound(null);
                }
            }
            if (usesNbtMatching) {
                return GTUtility.areStacksEqual(unifiedStack, other, false);
            } else {
                return GTOreDictUnificator.isInputStackEqual(other, oredictOther, unifiedStack);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
