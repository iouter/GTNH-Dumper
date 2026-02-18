package com.iouter.gtnhdumper.common.recipe;

import codechicken.nei.recipe.GuiRecipeTab;
import codechicken.nei.recipe.HandlerInfo;
import codechicken.nei.recipe.RecipeCatalysts;
import com.google.common.base.Objects;
import com.gtnewhorizons.modularui.api.drawable.FallbackableUITexture;
import com.iouter.gtnhdumper.Utils;
import com.iouter.gtnhdumper.common.recipe.base.GTRecipeDumps;
import com.iouter.gtnhdumper.common.recipe.base.RecipeFluid;
import com.iouter.gtnhdumper.common.recipe.base.RecipeItem;
import com.iouter.gtnhdumper.common.recipe.utils.Transformer;
import gregtech.api.enums.Materials;
import gregtech.api.recipe.BasicUIProperties;
import gregtech.api.recipe.RecipeCategory;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMapBackend;
import gregtech.api.recipe.RecipeMaps;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Collectors;

public class GTDefaultHandlerRecipe{
    private final String name;
    private final String identifier;
    private final String source;
    private final String markedItem;
    private final ArrayList<String> catalysts;
    private final String progressBar;
    private Integer amperage;
    private final ArrayList<GTRecipeDumps> recipes;

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

        RecipeCatalysts.getRecipeCatalysts(handler).forEach(positionedStack -> {
            ItemStack[] items = positionedStack.items;
            if (items == null)
                return;
            for (ItemStack stack: items) {
                catalysts.add(Utils.getItemKeyWithNBT(stack));
            }
        });

        this.progressBar = Utils.getAfterLastChar(getProgressBar(getUIProperties(handler)), '/');
        RecipeCategory category = getRecipeCategory(handler);
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
                    recipes.add(new GTRecipeDumps(
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
        gtRecipes.stream().filter(gtRecipe -> gtRecipe.getRecipeCategory() == category).map(Transformer::transformGTRecipe).forEach(recipes::add);
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
}
