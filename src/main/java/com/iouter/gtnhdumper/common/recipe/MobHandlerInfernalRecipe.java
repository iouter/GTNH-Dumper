package com.iouter.gtnhdumper.common.recipe;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.iouter.gtnhdumper.GTNHDumper;
import com.iouter.gtnhdumper.common.recipe.base.BaseHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.base.RecipeItem;
import com.iouter.gtnhdumper.common.utils.RecipeUtil;
import com.kuba6000.mobsinfo.nei.MobHandlerInfernal;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MobHandlerInfernalRecipe extends BaseHandlerRecipe {

    private static final Class<?> infernalRecipeClass;
    private static final Class<?> infernalPositionedStackClass;

    private static final Field eliteChanceField;
    private static final Field ultraChanceField;
    private static final Field infernoChanceField;
    private static final Field eliteField;
    private static final Field ultraField;
    private static final Field infernoField;

    private static final Field chanceField;
    private static final Field chanceAlwaysField;

    static {
        try {
            infernalRecipeClass = Class.forName("com.kuba6000.mobsinfo.nei.MobHandlerInfernal$InfernalRecipe");
            infernalPositionedStackClass = Class
                .forName("com.kuba6000.mobsinfo.nei.MobHandlerInfernal$InfernalPositionedStack");

            eliteChanceField = infernalRecipeClass.getDeclaredField("eliteChance");
            ultraChanceField = infernalRecipeClass.getDeclaredField("ultraChance");
            infernoChanceField = infernalRecipeClass.getDeclaredField("infernoChance");
            eliteField = infernalRecipeClass.getDeclaredField("elite");
            ultraField = infernalRecipeClass.getDeclaredField("ultra");
            infernoField = infernalRecipeClass.getDeclaredField("inferno");

            chanceField = infernalPositionedStackClass.getDeclaredField("chance");
            chanceAlwaysField = infernalPositionedStackClass.getDeclaredField("chanceAlways");

            eliteChanceField.setAccessible(true);
            ultraChanceField.setAccessible(true);
            infernoChanceField.setAccessible(true);
            eliteField.setAccessible(true);
            ultraField.setAccessible(true);
            infernoField.setAccessible(true);
            chanceField.setAccessible(true);
            chanceAlwaysField.setAccessible(true);

        } catch (Exception e) {
            GTNHDumper.LOG.error(e);
            throw new RuntimeException(e);
        }
    }

    public static double getEliteChance(Object infernalRecipeObj) {
        try {
            return eliteChanceField.getDouble(infernalRecipeObj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static double getUltraChance(Object infernalRecipeObj) {
        try {
            return ultraChanceField.getDouble(infernalRecipeObj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static double getInfernoChance(Object infernalRecipeObj) {
        try {
            return infernoChanceField.getDouble(infernalRecipeObj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<PositionedStack> getElite(Object infernalRecipeObj) {
        try {
            return (List<PositionedStack>) eliteField.get(infernalRecipeObj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<PositionedStack> getUltra(Object infernalRecipeObj) {
        try {
            return (List<PositionedStack>) ultraField.get(infernalRecipeObj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<PositionedStack> getInferno(Object infernalRecipeObj) {
        try {
            return (List<PositionedStack>) infernoField.get(infernalRecipeObj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static double getChance(Object infernalPosStackObj) {
        try {
            return chanceField.getDouble(infernalPosStackObj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static double getChanceAlways(Object infernalPosStackObj) {
        try {
            return chanceAlwaysField.getDouble(infernalPosStackObj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public MobHandlerInfernalRecipe(MobHandlerInfernal handler) {
        super(handler);
    }

    @Override
    public List<?> getRecipes(IRecipeHandler baseHandler) {
        MobHandlerInfernal handler = (MobHandlerInfernal) baseHandler;
        List<MobInfernalRecipe> recipes = new ArrayList<>();
        handler.loadCraftingRecipes(handler.getOverlayIdentifier(), (Object) null);
        for (TemplateRecipeHandler.CachedRecipe cachedRecipe : handler.arecipes) {
            if (!infernalRecipeClass.isInstance(cachedRecipe)) {
                continue;
            }
            double eliteChance = getEliteChance(cachedRecipe);
            double ultraChance = getUltraChance(cachedRecipe);
            double infernoChance = getInfernoChance(cachedRecipe);
            recipes.add(
                new MobInfernalRecipe().setEliteChance(eliteChance)
                    .setEliteItems(
                        getElite(cachedRecipe).stream()
                            .map(MobHandlerInfernalRecipe::toMobItem)
                            .toArray(Object[]::new))
                    .setUltraChance(eliteChance * ultraChance)
                    .setUltraChanceAlways(ultraChance)
                    .setUltraItems(
                        getUltra(cachedRecipe).stream()
                            .map(MobHandlerInfernalRecipe::toMobItem)
                            .toArray(Object[]::new))
                    .setInfernoChance(eliteChance * ultraChance * infernoChance)
                    .setInfernoChanceAlways(ultraChance * infernoChance)
                    .setInfernoItems(
                        getInferno(cachedRecipe).stream()
                            .map(MobHandlerInfernalRecipe::toMobItem)
                            .toArray(Object[]::new)));
        }
        return recipes;
    }

    private static MobInfernalItem transferToMobItem(RecipeItem item, PositionedStack mobStack, boolean enchantable) {
        MobInfernalItem mob = new MobInfernalItem(item);
        if (!infernalPositionedStackClass.isInstance(mobStack)) {
            return mob;
        }
        return mob.setChance(getChance(mobStack))
            .setChanceAlways(getChanceAlways(mobStack))
            .setEnchantable(enchantable);
    }

    private static Object toMobItem(PositionedStack mobStack) {
        Object obj = RecipeUtil.getRecipeItems(mobStack);
        boolean enchantable = Objects.requireNonNull(mobStack.items[0].getItem())
            .getItemEnchantability() > 0;
        if (obj instanceof RecipeItem item) {
            return transferToMobItem(item, mobStack, enchantable);
        } else if (obj instanceof RecipeItem[]recipeItems) {
            return Arrays.stream(recipeItems)
                .map(recipeItem -> transferToMobItem(recipeItem, mobStack, enchantable))
                .toArray(MobInfernalItem[]::new);
        } else {
            return null;
        }
    }

    @Setter
    @Accessors(chain = true)
    private static class MobInfernalRecipe {

        private double eliteChance;
        private double eliteChanceAlways = 1;
        private Object[] eliteItems;
        private double ultraChance;
        private double ultraChanceAlways;
        private Object[] ultraItems;
        private double infernoChance;
        private double infernoChanceAlways;
        private Object[] infernoItems;
    }

    @Setter
    @Accessors(chain = true)
    private static class MobInfernalItem extends RecipeItem {

        private double chanceAlways;
        private boolean enchantable;

        public MobInfernalItem(String key, int amount) {
            super(key, amount);
        }

        public MobInfernalItem(ItemStack stack) {
            super(stack);
        }

        public MobInfernalItem(RecipeItem recipeItem) {
            this(recipeItem.key, 1);
            this.amount = recipeItem.amount;
            this.chance = recipeItem.chance;
            this.nbt = recipeItem.nbt;
            this.tooltip = recipeItem.tooltip;
        }

        public MobInfernalItem setChance(double chance) {
            this.chance = chance;
            return this;
        }
    }
}
