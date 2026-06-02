package com.iouter.gtnhdumper.common.utils;

import codechicken.nei.NEIServerUtils;
import com.iouter.gtnhdumper.common.recipe.GTDefaultHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.base.RecipeFluid;
import com.iouter.gtnhdumper.common.recipe.base.RecipeItem;
import gregtech.api.recipe.RecipeMetadataKey;
import gregtech.api.recipe.metadata.IRecipeMetadataStorage;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTRecipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Transformer {

    private Transformer() {}

    public static GTDefaultHandlerRecipe.GTDumpedRecipe transformGTRecipe(GTRecipe src) {
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
        if (src.mSpecialItems instanceof ItemStack temp) {
            otherItems.add(new RecipeItem(temp));
        }
        if (otherItems.isEmpty()) otherItems = null;
        IRecipeMetadataStorage metadataStorage = src.getMetadataStorage();
        Map<String, Object> metadata = new LinkedHashMap<>();
        try {
            Class<?> clazz = RecipeMetadataKey.class;
            Field idField = clazz.getDeclaredField("identifier");
            idField.setAccessible(true);
            for (Map.Entry<RecipeMetadataKey<?>, Object> meta : metadataStorage.getEntries()) {
                try {
                    Object value = meta.getValue();
                    String key = idField.get(meta.getKey())
                        .toString();
                    metadata.put(key, value);
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            metadata = null;
        }
        if (metadata != null && metadata.isEmpty()) metadata = null;
        return new GTDefaultHandlerRecipe.GTDumpedRecipe(
            inputItems,
            inputFluids,
            outputItems,
            outputFluids,
            otherItems,
            src.mEUt,
            src.mDuration,
            src.mSpecialValue,
            metadata);
    }

    public static ArrayList<Object> getInputItems(GTRecipe gtRecipe) {
        ArrayList<Object> inputItems = new ArrayList<>();
        for (int i = 0; i < gtRecipe.mInputs.length; i++) {
            int chance = gtRecipe.getInputChance(i);
            if (gtRecipe instanceof GTRecipe.GTRecipe_WithAlt gtRecipeWithAlt) {
                Object stackObj = gtRecipeWithAlt.getAltRepresentativeInput(i);
                if (stackObj instanceof ItemStack stackAlt) {
                    inputItems.add(new RecipeItem(stackAlt).withChance(chance));
                } else if (stackObj instanceof ItemStack[]stacks) {
                    inputItems.add(RecipeUtil.getRecipeItems(stacks, chance));
                }
            } else {
                ItemStack stack = gtRecipe.mInputs[i];
                if (stack == null) continue;
                inputItems.add(
                    RecipeUtil.getRecipeItems(
                        NEIServerUtils.extractRecipeItems(GTOreDictUnificator.getNonUnifiedStacks(stack)),
                        chance));
            }
        }
        if (inputItems.isEmpty()) return null;
        return inputItems;
    }

    public static ArrayList<Object> getOutputItems(GTRecipe gtRecipe) {
        ArrayList<Object> outputItems = new ArrayList<>();
        for (int i = 0; i < gtRecipe.mOutputs.length; i++) {
            outputItems.add(new RecipeItem(gtRecipe.mOutputs[i]).withChance(gtRecipe.getOutputChance(i)));
        }
        if (outputItems.isEmpty()) return null;
        return outputItems;
    }

    public static ArrayList<RecipeFluid> getInputFluids(GTRecipe gtRecipe) {
        ArrayList<RecipeFluid> inputFluids = new ArrayList<>();
        for (int i = 0; i < gtRecipe.mFluidInputs.length; i++) {
            final FluidStack stack = gtRecipe.mFluidInputs[i];
            inputFluids.add(
                new RecipeFluid(stack).withNBT(stack)
                    .withChance(gtRecipe.getFluidInputChance(i)));
        }
        if (inputFluids.isEmpty()) return null;
        return inputFluids;
    }

    public static ArrayList<RecipeFluid> getOutputFluids(GTRecipe gtRecipe) {
        ArrayList<RecipeFluid> outputFluids = new ArrayList<>();
        for (int i = 0; i < gtRecipe.mFluidOutputs.length; i++) {
            final FluidStack stack = gtRecipe.mFluidOutputs[i];
            outputFluids.add(
                new RecipeFluid(stack).withNBT(stack)
                    .withChance(gtRecipe.getFluidOutputChance(i)));
        }
        if (outputFluids.isEmpty()) return null;
        return outputFluids;
    }
}
