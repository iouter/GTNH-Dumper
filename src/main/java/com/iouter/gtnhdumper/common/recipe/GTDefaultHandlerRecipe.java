package com.iouter.gtnhdumper.common.recipe;

import codechicken.nei.recipe.GuiRecipeTab;
import codechicken.nei.recipe.HandlerInfo;
import codechicken.nei.recipe.RecipeCatalysts;
import com.google.common.base.Objects;
import com.gtnewhorizons.modularui.api.drawable.FallbackableUITexture;
import com.iouter.gtnhdumper.Utils;
import com.iouter.gtnhdumper.common.recipe.base.RecipeFluid;
import com.iouter.gtnhdumper.common.recipe.base.RecipeItem;
import gregtech.api.objects.ItemData;
import gregtech.api.recipe.BasicUIProperties;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMapBackend;
import gregtech.api.recipe.RecipeMetadataKey;
import gregtech.api.recipe.metadata.IRecipeMetadataStorage;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTRecipe;
import gregtech.nei.GTNEIDefaultHandler;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class GTDefaultHandlerRecipe{
    private final String name;
    private final String markedItem;
    private final ArrayList<String> catalysts;
    private final String progressBar;
    private Integer amperage;
    private final ArrayList<com.iouter.gtnhdumper.common.recipe.base.GTRecipe> recipes;

    public GTDefaultHandlerRecipe(GTNEIDefaultHandler handler) {
        this.name = handler.getRecipeName();
        this.catalysts = new ArrayList<>();

        System.out.println("正在导出：" + name);

        final String handlerName = handler.getHandlerId();
        final String handlerId = Objects.firstNonNull(
            handler.getOverlayIdentifier(),
            "null");
        HandlerInfo info = GuiRecipeTab.getHandlerInfo(handlerName, handlerId);
        final ItemStack markedItemStack = info != null ? info.getItemStack() : null;
        this.markedItem = markedItemStack != null ? Utils.getItemKeyWithNBT(markedItemStack) : "null";

        RecipeCatalysts.getRecipeCatalysts(handler).stream().forEach(positionedStack -> {
            ItemStack[] items = positionedStack.items;
            if (items == null)
                return;
            for (ItemStack stack: items) {
                catalysts.add(Utils.getItemKeyWithNBT(stack));
            }
        });

        this.progressBar = Utils.getAfterLastChar(getProgressBar(getUIProperties(handler)), '/');
        RecipeMap<?> recipeMap = handler.getRecipeMap();
        int amperage = recipeMap.getAmperage();
        if (amperage > 1)
            this.amperage = amperage;
        RecipeMapBackend recipeMapBackend = recipeMap.getBackend();
        Collection<GTRecipe> gtRecipes = recipeMapBackend.getAllRecipes();
        recipes = new ArrayList<>();
        for (GTRecipe gtRecipe : gtRecipes) {
            ArrayList<RecipeItem> inputItems = new ArrayList<>();
            for (int i = 0; i < gtRecipe.mInputs.length; i++) {
                ItemStack stack = gtRecipe.mInputs[i];
                if (stack == null)
                    continue;
                ItemData data = GTOreDictUnificator.getAssociation(stack);
                if (data == null)
                    inputItems.add(new RecipeItem(stack).withNBT(stack));
                else
                    inputItems.add(new RecipeItem("#" + data, stack.stackSize).withNBT(stack));
            }
            if (inputItems.isEmpty())
                inputItems = null;
            ArrayList<RecipeFluid> inputFluids = Arrays.stream(gtRecipe.mFluidInputs).map(fluidStack -> new RecipeFluid(fluidStack).withNBT(fluidStack)).collect(Collectors.toCollection(ArrayList::new));
            if (inputFluids.isEmpty())
                inputFluids = null;
            ArrayList<RecipeItem> outputItems = new ArrayList<>();
            for (int i = 0; i < gtRecipe.mOutputs.length; i++) {
                outputItems.add(new RecipeItem(gtRecipe.mOutputs[i]).withNBT(gtRecipe.mOutputs[i]).withChance(gtRecipe.getOutputChance(i)));
            }
            if (outputItems.isEmpty())
                outputItems = null;
            ArrayList<RecipeFluid> outputFluids = Arrays.stream(gtRecipe.mFluidOutputs).map(fluidStack -> new RecipeFluid(fluidStack).withNBT(fluidStack)).collect(Collectors.toCollection(ArrayList::new));
            if (outputFluids.isEmpty())
                outputFluids = null;
            ArrayList<RecipeItem> otherItems = new ArrayList<>();
            if (gtRecipe.mSpecialItems instanceof ItemStack) {
                otherItems.add(new RecipeItem((ItemStack) gtRecipe.mSpecialItems));
            }
            if (otherItems.isEmpty())
                otherItems = null;
            IRecipeMetadataStorage metadataStorage = gtRecipe.getMetadataStorage();
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
            recipes.add(new com.iouter.gtnhdumper.common.recipe.base.GTRecipe(
                inputItems,
                inputFluids,
                outputItems,
                outputFluids,
                otherItems,
                gtRecipe.mEUt,
                gtRecipe.mDuration,
                gtRecipe.mSpecialValue,
                metadata
            ));
        }
    }

    public static BasicUIProperties getUIProperties(GTNEIDefaultHandler handler) {
        try {
            Class<?> clazz = handler.getClass();
            Field field = clazz.getDeclaredField("uiProperties");
            field.setAccessible(true);
            return (BasicUIProperties) field.get(handler);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getProgressBar(BasicUIProperties ui) {
        if (ui == null)
            return null;
        final FallbackableUITexture texture = ui.progressBarTexture;
        if (texture == null)
            return null;
        return texture.get().location.toString();
    }
}
