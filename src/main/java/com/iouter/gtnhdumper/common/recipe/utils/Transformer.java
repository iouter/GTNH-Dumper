package com.iouter.gtnhdumper.common.recipe.utils;

import codechicken.nei.NEIServerUtils;
import com.iouter.gtnhdumper.GTNHDumper;
import com.iouter.gtnhdumper.common.recipe.base.GTRecipeDumps;
import com.iouter.gtnhdumper.common.recipe.base.RecipeFluid;
import com.iouter.gtnhdumper.common.recipe.base.RecipeItem;
import com.iouter.gtnhdumper.common.recipe.base.RecipeUtil;
import gregtech.api.recipe.RecipeMetadataKey;
import gregtech.api.recipe.metadata.IRecipeMetadataStorage;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTRecipe;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Transformer {
    private Transformer() {}

    public static GTRecipeDumps transformGTRecipe(GTRecipe src) {
        if (src == null) {
            return null;
        }
        if (src.mHidden) {
            return null;
        }
        ArrayList<Object> inputItems = getInputItems(src);
        ArrayList<RecipeFluid> inputFluids = getInputFluids(src);
        ArrayList<Object> outputItems = getOutputItems(src);
        ArrayList<RecipeFluid> outputFluids = getOutputFluids(src);
        ArrayList<Object> otherItems = new ArrayList<>();
        if (src.mSpecialItems instanceof ItemStack) {
            ItemStack temp = (ItemStack) src.mSpecialItems;
            otherItems.add(new RecipeItem(temp));
        }
        if (otherItems.isEmpty())
            otherItems = null;
        IRecipeMetadataStorage metadataStorage = src.getMetadataStorage();
        Map<String, Object> metadata = new HashMap<>();
        try {
            Class<?> clazz = RecipeMetadataKey.class;
            Field idField = clazz.getDeclaredField("identifier");
            idField.setAccessible(true);
            for (Map.Entry<RecipeMetadataKey<?>, Object> meta: metadataStorage.getEntries()) {
                try {
                    Object value = meta.getValue();
                    String key = idField.get(meta.getKey()).toString();
                    metadata.put(key, value);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            metadata = null;
        }
        if (metadata != null && metadata.isEmpty())
            metadata = null;
        return new GTRecipeDumps(
            inputItems,
            inputFluids,
            outputItems,
            outputFluids,
            otherItems,
            src.mEUt,
            src.mDuration,
            src.mSpecialValue,
            metadata
        );
    }

    public static ArrayList<Object> getInputItems(GTRecipe gtRecipe) {
        ArrayList<Object> inputItems = new ArrayList<>();
        for (int i = 0; i < gtRecipe.mInputs.length; i++) {
            if (gtRecipe instanceof GTRecipe.GTRecipe_WithAlt) {
                GTRecipe.GTRecipe_WithAlt gtRecipeWithAlt = (GTRecipe.GTRecipe_WithAlt) gtRecipe;
                Object stackObj = gtRecipeWithAlt.getAltRepresentativeInput(i);
                if (stackObj instanceof ItemStack) {
                    ItemStack stackAlt = (ItemStack) stackObj;
                    inputItems.add(new RecipeItem(stackAlt));
                } else if (stackObj instanceof ItemStack[]) {
                    ItemStack[] stacks = (ItemStack[]) stackObj;
                    inputItems.add(RecipeUtil.getRecipeItems(stacks));
                }
            } else {
                ItemStack stack = gtRecipe.mInputs[i];
                if (stack == null)
                    continue;
                inputItems.add(RecipeUtil.getRecipeItems(NEIServerUtils.extractRecipeItems(GTOreDictUnificator.getNonUnifiedStacks(stack))));
            }
        }
        if (inputItems.isEmpty())
            return null;
        return inputItems;
    }

    public static ArrayList<Object> getOutputItems(GTRecipe gtRecipe) {
        ArrayList<Object> outputItems = new ArrayList<>();
        for (int i = 0; i < gtRecipe.mOutputs.length; i++) {
            outputItems.add(new RecipeItem(gtRecipe.mOutputs[i]).withChance(gtRecipe.getOutputChance(i)));
        }
        if (outputItems.isEmpty())
            return null;
        return outputItems;
    }

    public static ArrayList<RecipeFluid> getInputFluids(GTRecipe gtRecipe) {
        ArrayList<RecipeFluid> inputFluids = Arrays.stream(gtRecipe.mFluidInputs).map(fluidStack -> new RecipeFluid(fluidStack).withNBT(fluidStack)).collect(Collectors.toCollection(ArrayList::new));
        if (inputFluids.isEmpty())
            return null;
        return inputFluids;
    }

    public static ArrayList<RecipeFluid> getOutputFluids(GTRecipe gtRecipe) {
        ArrayList<RecipeFluid> outputFluids = Arrays.stream(gtRecipe.mFluidOutputs).map(fluidStack -> new RecipeFluid(fluidStack).withNBT(fluidStack)).collect(Collectors.toCollection(ArrayList::new));
        if (outputFluids.isEmpty())
            return null;
        return outputFluids;
    }
}
