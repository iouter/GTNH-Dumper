package com.iouter.gtnhdumper;

import codechicken.lib.inventory.InventoryUtils;
import codechicken.nei.PositionedStack;
import com.iouter.gtnhdumper.common.recipe.base.RecipeItem;
import com.iouter.gtnhdumper.common.recipe.base.RecipeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Utils {
    public static final Minecraft minecraft = Minecraft.getMinecraft();

    public static String getItemKeyWithNBT(ItemStack stack) {
        final String nbt = getItemNBT(stack);
        final String key = getItemKey(stack);
        if (nbt != null)
            return key + nbt;
        return key;
    }

    public static String getItemKey(ItemStack stack) {
        if (stack == null)
            return "null";
        final String name = Item.itemRegistry.getNameForObject(stack.getItem());
        final int meta = InventoryUtils.actualDamage(stack);
        if (meta != 0)
            return name + ":" + meta;
        return name;
    }

    public static String getItemNBT(ItemStack stack) {
        if (stack == null)
            return null;
        NBTTagCompound nbt = stack.stackTagCompound;
        if (nbt == null)
            return null;
        return nbt.toString();
    }

    public static String getFluidNBT(FluidStack stack) {
        if (stack == null)
            return null;
        NBTTagCompound nbt = stack.tag;
        if (nbt == null)
            return null;
        return nbt.toString();
    }

    public static ArrayList<ItemStack[]> getItemStacks(List<PositionedStack> positionedStacks) {
        ArrayList<ItemStack[]> stacks = new ArrayList<>();
        if (positionedStacks == null)
            return stacks;
        for (PositionedStack positionedStack : positionedStacks) {
            if (positionedStack.items == null)
                continue;
            ItemStack[] items = positionedStack.items;
            stacks.add(items);
        }
        return stacks;
    }

    public static ArrayList<ItemStack[]> getItemStacks(PositionedStack positionedStacks) {
        ArrayList<ItemStack[]> list = new ArrayList<>();
        if (positionedStacks == null || positionedStacks.items == null)
            return list;
        list.add(positionedStacks.items);
        return list;
    }

    public static ArrayList<Object> getRecipeItems(ArrayList<ItemStack[]> itemStacks) {
        ArrayList<Object> list = new ArrayList<>();
        for (ItemStack[] stack : itemStacks) {
            if (stack == null)
                continue;
            Object recipeItem = RecipeUtil.getRecipeItems(stack);
            list.add(recipeItem);
        }
        return list;
    }

    public static ArrayList<Object> getRecipeItems(List<PositionedStack> positionedStacks) {
        return getRecipeItems(getItemStacks(positionedStacks));
    }

    public static ArrayList<Object> getRecipeItems(PositionedStack positionedStack) {
        return getRecipeItems(getItemStacks(positionedStack));
    }

    public static Map<String, String[]> getOreDict() {
        Map<String, String[]> map= new HashMap<>();
        try {
            Class<?> oreDictClass = Class.forName("net.minecraftforge.oredict.OreDictionary");

            Field idToNameField = oreDictClass.getDeclaredField("idToName");
            idToNameField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<String> idToName = (List<String>) idToNameField.get(null);

            Field idToStackField = oreDictClass.getDeclaredField("idToStack");
            idToStackField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<ArrayList<ItemStack>> idToStack = (List<ArrayList<ItemStack>>) idToStackField.get(null);

            for (int i = 0; i < idToName.size(); i++) {
                if (i >= idToStack.size())
                    continue;
                String name = idToName.get(i);
                ArrayList<ItemStack> items= idToStack.get(i);
                if (name == null || items == null || items.isEmpty())
                    continue;
                map.put(name, items.stream().map(Utils::getItemKey).toArray(String[]::new));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static String getOreDictByItems(ItemStack[] stacks, Map<String, String[]> map) {
        if (stacks == null || stacks.length == 0 || map == null || map.isEmpty()) {
            return null;
        }
        String[] stackKeys = Arrays.stream(stacks)
            .map(Utils::getItemKey)
            .toArray(String[]::new);
        Map<String, Long> stackFreq = Arrays.stream(stackKeys)
            .collect(Collectors.groupingBy(
                Function.identity(),
                Collectors.counting()
            ));
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            String[] dictArray = entry.getValue();
            if (dictArray == null || dictArray.length != stackKeys.length) {
                continue;
            }
            Map<String, Long> dictFreq = Arrays.stream(dictArray)
                .collect(Collectors.groupingBy(
                    Function.identity(),
                    Collectors.counting()
                ));

            if (stackFreq.equals(dictFreq)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static int getCraftingSerial(PositionedStack s) {
        return getCraftingSerial(s.relx, s.rely);
    }

    public static int getCraftingSerial(int x, int y) {
        int serialY = getCraftingSerialY(y);
        switch (x) {
            case 25:
                return 1 + serialY;
            case 43:
                return 2 + serialY;
            case 61:
                return 3 + serialY;
            default:
                return -1;
        }
    }

    private static int getCraftingSerialY(int y) {
        switch (y) {
            case 6:
                return 0;
            case 24:
                return 3;
            case 42:
                return 6;
            default:
                return -1;
        }
    }

    public static String getAfterLastChar(String input, char c) {
        return Optional.ofNullable(input)
            .map(str -> {
                int lastDotIndex = str.lastIndexOf(c);
                if (lastDotIndex == -1) {
                    return str;
                }
                return str.substring(lastDotIndex + 1);
            })
            .orElse(null);
    }

    public static String getAfterLastDot(String input) {
        return getAfterLastChar(input, '.');
    }

    public static String getTooltip(ItemStack itemStack) {
        try {
            List<String> tooltips = itemStack.getTooltip(minecraft.thePlayer, false);
            return String.join("<br>", tooltips);
        } catch (Exception e) {
            return "ERROR";
        }
    }

    public static boolean isItemStackEqual(ItemStack a, ItemStack b) {
        if (a == null) {
            return b == null;
        }
        final boolean nameEqual = Item.itemRegistry.getNameForObject(a.getItem()).equals(Item.itemRegistry.getNameForObject(b.getItem()));
        final boolean metaEqual = InventoryUtils.actualDamage(a) == InventoryUtils.actualDamage(b);
        final boolean sizeEqual = a.stackSize == b.stackSize;
        final boolean nbtEqual = Objects.equals(a.stackTagCompound, b.stackTagCompound);
        return nameEqual && metaEqual && sizeEqual && nbtEqual;
    }

    public static boolean isStacksContain(ItemStack s, ItemStack[] stacks) {
        for (ItemStack stack : stacks) {
            if (isItemStackEqual(s, stack)) {
                return true;
            }
        }
        return false;
    }
}
