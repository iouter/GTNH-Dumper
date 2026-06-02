package com.iouter.gtnhdumper.common.recipe;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler.CachedRecipe;
import com.iouter.gtnhdumper.GTNHDumper;
import com.iouter.gtnhdumper.common.recipe.base.BaseHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.base.RecipeItem;
import com.iouter.gtnhdumper.common.utils.RecipeUtil;
import com.kuba6000.mobsinfo.api.SpawnInfo;
import com.kuba6000.mobsinfo.nei.MobHandler;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class MobHandlerRecipe extends BaseHandlerRecipe {

    private static final Class<?> mobCachedRecipeClass;
    private static final Field mobField;
    private static final Field mobnameField;
    private static final Field localizedNameField;
    private static final Field mOutputsField;
    private static final Field mInputField;
    private static final Field infernaltypeField;
    private static final Field ingredientField;
    private static final Field modField;
    private static final Field maxHealthField;
    private static final Field isUsableInVialField;
    private static final Field isPeacefulAllowedField;
    private static final Field additionalInformationField;
    private static final Field spawnListField;
    private static final Field isBossField;

    static {
        try {
            mobCachedRecipeClass = Class.forName("com.kuba6000.mobsinfo.nei.MobHandler$MobCachedRecipe");
            mobField = mobCachedRecipeClass.getField("mob");
            mobnameField = mobCachedRecipeClass.getField("mobname");
            localizedNameField = mobCachedRecipeClass.getField("localizedName");
            mOutputsField = mobCachedRecipeClass.getField("mOutputs");
            mInputField = mobCachedRecipeClass.getField("mInput");
            infernaltypeField = mobCachedRecipeClass.getField("infernaltype");
            ingredientField = mobCachedRecipeClass.getField("ingredient");
            modField = mobCachedRecipeClass.getField("mod");
            maxHealthField = mobCachedRecipeClass.getField("maxHealth");
            isUsableInVialField = mobCachedRecipeClass.getField("isUsableInVial");
            isPeacefulAllowedField = mobCachedRecipeClass.getField("isPeacefulAllowed");
            additionalInformationField = mobCachedRecipeClass.getField("additionalInformation");
            spawnListField = mobCachedRecipeClass.getField("spawnList");
            isBossField = mobCachedRecipeClass.getField("isBoss");

            mobField.setAccessible(true);
            mobnameField.setAccessible(true);
            localizedNameField.setAccessible(true);
            mOutputsField.setAccessible(true);
            mInputField.setAccessible(true);
            infernaltypeField.setAccessible(true);
            ingredientField.setAccessible(true);
            modField.setAccessible(true);
            maxHealthField.setAccessible(true);
            isUsableInVialField.setAccessible(true);
            isPeacefulAllowedField.setAccessible(true);
            additionalInformationField.setAccessible(true);
            spawnListField.setAccessible(true);
            isBossField.setAccessible(true);
        } catch (Exception e) {
            GTNHDumper.LOG.error(e);
            throw new RuntimeException(e);
        }
    }

    public MobHandlerRecipe(MobHandler handler) {
        super(handler);
    }

    @Override
    public List<?> getRecipes(IRecipeHandler baseHandler) {
        List<MobRecipe> recipes = new ArrayList<>();
        MobHandler handler = (MobHandler) baseHandler;
        handler.loadCraftingRecipes(handler.getOverlayIdentifier(), (Object) null);
        for (CachedRecipe cachedRecipe : handler.arecipes) {
            if (!mobCachedRecipeClass.isInstance(cachedRecipe)) {
                continue;
            }
            String[] spawnList;
            if (getSpawnList(cachedRecipe).isEmpty()) {
                spawnList = null;
            } else {
                spawnList = getSpawnList(cachedRecipe).stream()
                    .map(SpawnInfo::getInfo)
                    .toArray(String[]::new);
            }
            List<Object> normal = new ArrayList<>();
            List<Object> rare = new ArrayList<>();
            List<Object> additional = new ArrayList<>();
            List<Object> infernal = new ArrayList<>();
            List<PositionedStack> drops = getMOutputs(cachedRecipe);
            for (PositionedStack positionedStack : drops) {
                if (!(positionedStack instanceof MobHandler.MobPositionedStack mobStack)) {
                    continue;
                }
                Object mobs = toMobItem(mobStack);
                switch (mobStack.type) {
                    case Normal: {
                        normal.add(mobs);
                        break;
                    }
                    case Rare: {
                        rare.add(mobs);
                        break;
                    }
                    case Additional: {
                        additional.add(mobs);
                        break;
                    }
                    case Infernal: {
                        infernal.add(mobs);
                        break;
                    }
                }
            }
            if (normal.isEmpty()) {
                normal = null;
            }
            if (rare.isEmpty()) {
                rare = null;
            }
            if (additional.isEmpty()) {
                additional = null;
            }
            if (infernal.isEmpty()) {
                infernal = null;
            }
            recipes.add(
                new MobRecipe().setEntityName(getMobname(cachedRecipe))
                    .setLocalizedName(getLocalizedName(cachedRecipe))
                    .setMod(getMod(cachedRecipe))
                    .setMaxHealth(getMaxHealth(cachedRecipe))
                    .setInfernalType(getInfernaltype(cachedRecipe))
                    .setSpawnList(spawnList)
                    .setBoss(!getIsBoss(cachedRecipe).isEmpty())
                    .setPeacefulAllowed(isPeacefulAllowed(cachedRecipe))
                    .setUsableInVial(isUsableInVial(cachedRecipe))
                    .setAdditionalInformation(
                        getAdditionalInformation(cachedRecipe).isEmpty() ? null
                            : getAdditionalInformation(cachedRecipe))
                    .setInputItems(getMInput(cachedRecipe))
                    .setNormalDrops(normal)
                    .setRareDrops(rare)
                    .setAdditionalDrops(additional)
                    .setInfernalDrops(infernal));
        }
        return recipes;
    }

    public static EntityLiving getMob(Object obj) {
        try {
            return (EntityLiving) mobField.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getMobname(Object obj) {
        try {
            return (String) mobnameField.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getLocalizedName(Object obj) {
        try {
            return (String) localizedNameField.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<PositionedStack> getMOutputs(Object obj) {
        try {
            return (List<PositionedStack>) mOutputsField.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<ItemStack> getMInput(Object obj) {
        try {
            return (List<ItemStack>) mInputField.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getInfernaltype(Object obj) {
        try {
            return infernaltypeField.getInt(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static PositionedStack getIngredient(Object obj) {
        try {
            return (PositionedStack) ingredientField.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getMod(Object obj) {
        try {
            return (String) modField.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static float getMaxHealth(Object obj) {
        try {
            return maxHealthField.getFloat(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isUsableInVial(Object obj) {
        try {
            return isUsableInVialField.getBoolean(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isPeacefulAllowed(Object obj) {
        try {
            return isPeacefulAllowedField.getBoolean(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<String> getAdditionalInformation(Object obj) {
        try {
            return (List<String>) additionalInformationField.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Set<SpawnInfo> getSpawnList(Object obj) {
        try {
            return (Set<SpawnInfo>) spawnListField.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getIsBoss(Object obj) {
        try {
            return (String) isBossField.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static MobItem transferToMobItem(RecipeItem item, MobHandler.MobPositionedStack mobStack) {
        MobItem mob = new MobItem(item);
        mob.setRandomdamage(mobStack.randomdamage);
        mob.setDamages(compressToRanges(mobStack.damages));
        mob.setEnchantable(mobStack.enchantable);
        if (mobStack.enchantable) {
            mob.setEnchantmentLevel(mobStack.enchantmentLevel);
        }
        if (mobStack.extraTooltip != null) {
            if (mob.tooltip == null) {
                mob.tooltip = new ArrayList<>();
            }
            mob.tooltip.addAll(mobStack.extraTooltip);
        }
        return mob;
    }

    private static Object toMobItem(MobHandler.MobPositionedStack mobStack) {
        Object obj = RecipeUtil.getRecipeItems(mobStack);
        if (obj instanceof RecipeItem item) {
            return transferToMobItem(item, mobStack);
        } else if (obj instanceof RecipeItem[]recipeItems) {
            return Arrays.stream(recipeItems)
                .map(recipeItem -> transferToMobItem(recipeItem, mobStack))
                .toArray(MobItem[]::new);
        } else {
            return null;
        }
    }

    public static List<Object> compressToRanges(List<Integer> arr) {
        if (arr == null || arr.isEmpty()) return null;

        Integer[] sorted = arr.stream()
            .distinct()
            .sorted()
            .toArray(Integer[]::new);
        List<Object> result = new ArrayList<>();
        int start = sorted[0];
        int end = sorted[0];

        for (int i = 1; i < sorted.length; i++) {
            if (sorted[i] == end + 1) {
                end = sorted[i];
            } else {
                addRangeOrNumber(result, start, end);
                start = sorted[i];
                end = sorted[i];
            }
        }
        addRangeOrNumber(result, start, end);
        return result;
    }

    private static void addRangeOrNumber(List<Object> list, int start, int end) {
        if (start == end) {
            list.add(start);
        } else {
            list.add(new int[] { start, end });
        }
    }

    @Setter
    @Accessors(chain = true)
    private static class MobRecipe {

        private String entityName;
        private String localizedName;
        private String mod;
        private float maxHealth;
        private int infernalType;
        private String[] spawnList;
        private boolean isBoss;
        private boolean isPeacefulAllowed;
        private boolean isUsableInVial;
        private List<String> additionalInformation;
        private List<ItemStack> inputItems;
        private List<Object> normalDrops;
        private List<Object> rareDrops;
        private List<Object> additionalDrops;
        private List<Object> infernalDrops;
    }

    @Setter
    public static class MobItem extends RecipeItem {

        private boolean enchantable;
        private boolean randomdamage;
        private List<Object> damages;
        private int enchantmentLevel;

        public MobItem(String key, int amount) {
            super(key, amount);
        }

        public MobItem(ItemStack stack) {
            super(stack);
        }

        public MobItem(RecipeItem recipeItem) {
            this(recipeItem.key, 1);
            this.amount = recipeItem.amount;
            this.chance = recipeItem.chance;
            this.nbt = recipeItem.nbt;
            this.tooltip = recipeItem.tooltip;
        }
    }
}
