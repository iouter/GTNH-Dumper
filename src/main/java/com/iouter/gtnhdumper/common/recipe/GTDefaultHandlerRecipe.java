package com.iouter.gtnhdumper.common.recipe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;

import com.google.gson.JsonObject;
import com.gtnewhorizons.modularui.api.drawable.FallbackableUITexture;
import com.iouter.gtnhdumper.GTNHDumper;
import com.iouter.gtnhdumper.common.recipe.base.BaseHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.base.RecipeFluid;
import com.iouter.gtnhdumper.common.recipe.base.RecipeItem;
import com.iouter.gtnhdumper.common.utils.Transformer;
import com.iouter.gtnhdumper.common.utils.Utils;

import codechicken.nei.recipe.IRecipeHandler;
import gregtech.api.enums.Materials;
import gregtech.api.recipe.BasicUIProperties;
import gregtech.api.recipe.RecipeCategory;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMapBackend;
import gregtech.api.util.GTRecipe;
import gregtech.nei.GTNEIDefaultHandler;
import tectech.TecTech;
import tectech.recipe.EyeOfHarmonyFrontend;
import tectech.recipe.EyeOfHarmonyRecipe;
import tectech.recipe.EyeOfHarmonyRecipeStorage;
import tectech.util.FluidStackLong;
import tectech.util.ItemStackLong;

public class GTDefaultHandlerRecipe extends BaseHandlerRecipe {

    public GTDefaultHandlerRecipe(GTNEIDefaultHandler handler) {
        super(handler);
    }

    @Override
    public void addJsonObject(JsonObject json, IRecipeHandler baseHandler) {
        if (!(baseHandler instanceof GTNEIDefaultHandler handler)) {
            return;
        }
        RecipeMap<?> recipeMap = handler.getRecipeMap();

        json.addProperty("progressBar", Utils.getAfterLastChar(getProgressBar(getUIProperties(handler)), '/'));

        int amperage = recipeMap.getAmperage();
        if (amperage > 1) json.addProperty("amperage", amperage);
    }

    @Override
    public List<?> getRecipes(IRecipeHandler baseHandler) {
        if (!(baseHandler instanceof GTNEIDefaultHandler handler)) {
            return null;
        }
        List<GTDumpedRecipe> recipes = new ArrayList<>();
        RecipeCategory category = getRecipeCategory(handler);
        RecipeMap<?> recipeMap = handler.getRecipeMap();
        RecipeMapBackend recipeMapBackend = recipeMap.getBackend();
        Collection<GTRecipe> gtRecipes = recipeMapBackend.getAllRecipes();
        if (recipeMap.getFrontend() instanceof EyeOfHarmonyFrontend) {
            try {
                EyeOfHarmonyRecipeStorage storage = TecTech.eyeOfHarmonyRecipeStorage;
                Class<?> clazz = EyeOfHarmonyRecipeStorage.class;
                Field recipeHashMapField = clazz.getDeclaredField("recipeHashMap");
                recipeHashMapField.setAccessible(true);
                @SuppressWarnings("unchecked")
                HashMap<String, EyeOfHarmonyRecipe> recipeHashMap = (HashMap<String, EyeOfHarmonyRecipe>) recipeHashMapField
                    .get(storage);
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
                    recipes.add(
                        new GTDumpedRecipe(
                            inputItems,
                            inputFluids,
                            outputItems,
                            outputFluids,
                            null,
                            0,
                            recipe.getRecipeTimeInTicks(),
                            0,
                            null));
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                GTNHDumper.LOG.error(e);
            }
            return null;
        }
        gtRecipes.stream()
            .filter(gtRecipe -> gtRecipe.getRecipeCategory() == category)
            .map(Transformer::transformGTRecipe)
            .forEach(recipes::add);
        return recipes;
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
        if (ui == null) return null;
        final FallbackableUITexture texture = ui.progressBarTexture;
        if (texture == null) return null;
        return texture.get().location.toString();
    }

    public static RecipeCategory getRecipeCategory(GTNEIDefaultHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("Handler cannot be null");
        }

        try {
            // 1. 获取字段对象（使用精确字段名）
            Field recipeCategoryField = GTNEIDefaultHandler.class.getDeclaredField("recipeCategory");

            // 2. 突破访问限制（处理protected修饰符）
            recipeCategoryField.setAccessible(true);

            // 3. 从handler实例中获取字段值
            Object value = recipeCategoryField.get(handler);

            // 4. 类型安全转换
            if (value == null) {
                return null; // 允许字段值为null
            }
            if (value instanceof RecipeCategory) {
                return (RecipeCategory) value;
            }
            throw new ClassCastException("Field value is not a RecipeCategory instance");

        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Critical error: recipeCategory field missing in GTNEIDefaultHandler", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access recipeCategory field", e);
        }
    }

    public static class GTDumpedRecipe {

        private final ArrayList<Object> inputItems;
        private final ArrayList<RecipeFluid> inputFluids;
        private final ArrayList<Object> outputItems;
        private final ArrayList<RecipeFluid> outputFluids;
        private final ArrayList<Object> otherItems;
        private final Integer eut;
        private final Long duration;
        private Integer specialValue;
        private final Map<String, Object> metadata;

        public GTDumpedRecipe(ArrayList<Object> inputItems, ArrayList<RecipeFluid> inputFluids,
            ArrayList<Object> outputItems, ArrayList<RecipeFluid> outputFluids, ArrayList<Object> otherItems, int eut,
            long duration, int specialValue, Map<String, Object> metadata) {
            this.inputItems = inputItems;
            this.inputFluids = inputFluids;
            this.outputItems = outputItems;
            this.outputFluids = outputFluids;
            this.otherItems = otherItems;
            this.eut = eut;
            this.duration = duration;
            this.metadata = metadata;
            if (specialValue != 0) this.specialValue = specialValue;
        }
    }
}
