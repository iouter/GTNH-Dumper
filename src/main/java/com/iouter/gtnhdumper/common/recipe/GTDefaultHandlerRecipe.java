package com.iouter.gtnhdumper.common.recipe;

import codechicken.nei.NEIServerUtils;
import codechicken.nei.recipe.GuiRecipeTab;
import codechicken.nei.recipe.HandlerInfo;
import codechicken.nei.recipe.RecipeCatalysts;
import com.google.common.base.Objects;
import com.gtnewhorizons.modularui.api.drawable.FallbackableUITexture;
import com.iouter.gtnhdumper.Utils;
import com.iouter.gtnhdumper.common.recipe.base.RecipeFluid;
import com.iouter.gtnhdumper.common.recipe.base.RecipeItem;
import com.iouter.gtnhdumper.common.recipe.base.RecipeUtil;
import gregtech.api.enums.Materials;
import gregtech.api.recipe.BasicUIProperties;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMapBackend;
import gregtech.api.recipe.RecipeMetadataKey;
import gregtech.api.recipe.metadata.IRecipeMetadataStorage;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTRecipe;
import gregtech.nei.GTNEIDefaultHandler;
import net.minecraft.item.ItemStack;
import tectech.TecTech;
import tectech.recipe.EyeOfHarmonyFrontend;
import tectech.recipe.EyeOfHarmonyRecipe;
import tectech.recipe.EyeOfHarmonyRecipeStorage;
import tectech.util.FluidStackLong;
import tectech.util.ItemStackLong;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.iouter.gtnhdumper.common.recipe.base.RecipeItem.oreDictMap;

public class GTDefaultHandlerRecipe{
    private final String name;
    private final String identifier;
    private final String source;
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

        this.identifier = handlerId;
        this.source = handlerName;

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
        if (recipeMap.getFrontend() instanceof EyeOfHarmonyFrontend) {
            try {
                EyeOfHarmonyRecipeStorage storage = TecTech.eyeOfHarmonyRecipeStorage;
                Class<?> clazz = EyeOfHarmonyRecipeStorage.class;
                Field recipeHashMapField = clazz.getDeclaredField("recipeHashMap");
                recipeHashMapField.setAccessible(true);
                @SuppressWarnings("unchecked")
                HashMap<String, EyeOfHarmonyRecipe> recipeHashMap =
                    (HashMap<String, EyeOfHarmonyRecipe>) recipeHashMapField.get(storage);
                for (EyeOfHarmonyRecipe recipe : recipeHashMap.values()) {
                    ItemStack planetItem = recipe.getRecipeTriggerItem()
                        .copy();
                    planetItem.stackSize = 0;
                    ArrayList<Object> inputItems = new ArrayList<>();
                    inputItems.add(new RecipeItem(planetItem));
                    ArrayList<RecipeFluid> inputFluids = new ArrayList<>();
                    inputFluids.add(new RecipeFluid(Materials.Hydrogen.getGas(0)));
                    inputFluids.add(new RecipeFluid(Materials.Helium.getGas(0)));
                    inputFluids.add(new RecipeFluid(Materials.RawStarMatter.getFluid(0)));
                    ArrayList<Object> outputItems = new ArrayList<>();
                    for (ItemStackLong itemStackLong : recipe.getOutputItems()) {
                        outputItems.add(new RecipeItem(itemStackLong.itemStack).withAmount(itemStackLong.stackSize));
                    }
                    ArrayList<RecipeFluid> outputFluids = new ArrayList<>();
                    for (FluidStackLong fluidStackLong : recipe.getOutputFluids()) {
                        outputFluids.add(new RecipeFluid(fluidStackLong.fluidStack).withAmount(fluidStackLong.amount));
                    }
                    recipes.add(new com.iouter.gtnhdumper.common.recipe.base.GTRecipe(
                        inputItems,
                        inputFluids,
                        outputItems,
                        outputFluids,
                        null,
                        0,
                        recipe.getRecipeTimeInTicks(),
                        0,
                        null
                    ));
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }

            return;
        }
        for (GTRecipe gtRecipe : gtRecipes) {
            if (gtRecipe.mHidden) {
                continue;
            }
            ArrayList<Object> inputItems = getInputItems(gtRecipe);
            ArrayList<RecipeFluid> inputFluids = getInputFluids(gtRecipe);
            ArrayList<Object> outputItems = getOutputItems(gtRecipe);
            ArrayList<RecipeFluid> outputFluids = getOutputFluids(gtRecipe);
            ArrayList<Object> otherItems = new ArrayList<>();
            if (gtRecipe.mSpecialItems instanceof ItemStack) {
                ItemStack temp = (ItemStack) gtRecipe.mSpecialItems;
                otherItems.add(new RecipeItem(temp));
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
