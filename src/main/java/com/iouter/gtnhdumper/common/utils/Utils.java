package com.iouter.gtnhdumper.common.utils;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iouter.gtnhdumper.CommonProxy;
import com.iouter.gtnhdumper.GTNHDumper;
import com.iouter.gtnhdumper.common.recipe.base.RecipeItem;
import com.iouter.gtnhdumper.common.serializer.AspectListSerializer;
import com.iouter.gtnhdumper.common.serializer.AspectSerializer;
import com.iouter.gtnhdumper.common.serializer.ElementSerializer;
import com.iouter.gtnhdumper.common.serializer.FluidStackSerializer;
import com.iouter.gtnhdumper.common.serializer.ItemStackSerializer;
import com.iouter.gtnhdumper.common.serializer.MaterialsSerializer;
import com.iouter.gtnhdumper.common.serializer.NBTTagCompoundSerializer;
import com.iouter.gtnhdumper.common.serializer.RecipeItemSerializer;
import com.iouter.gtnhdumper.common.serializer.SafeDoubleSerializer;

import codechicken.lib.inventory.InventoryUtils;
import codechicken.nei.PositionedStack;
import gregtech.api.enums.Element;
import gregtech.api.enums.Materials;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public class Utils {

    public static final Minecraft minecraft = Minecraft.getMinecraft();
    private static final char[] BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final int OUTPUT_LENGTH = 15;
    private static final BigInteger BASE = BigInteger.valueOf(BASE62.length);

    public static String getItemKeyWithNBT(ItemStack stack) {
        final String nbt = getItemNBT(stack);
        final String key = getItemKey(stack);
        if (nbt != null) return key + nbt;
        return key;
    }

    public static String getItemKey(ItemStack stack) {
        if (stack == null) return "null";
        final String name = Item.itemRegistry.getNameForObject(stack.getItem());
        final int meta = InventoryUtils.actualDamage(stack);
        if (meta != 0) return name + ":" + meta;
        return name;
    }

    public static String getItemNBT(ItemStack stack) {
        if (stack == null) return null;
        NBTTagCompound nbt = stack.stackTagCompound;
        if (nbt == null) return null;
        return nbt.toString();
    }

    public static String getFluidNBT(FluidStack stack) {
        if (stack == null) return null;
        NBTTagCompound nbt = stack.tag;
        if (nbt == null) return null;
        return nbt.toString();
    }

    public static ArrayList<ItemStack[]> getItemStacks(List<PositionedStack> positionedStacks) {
        ArrayList<ItemStack[]> stacks = new ArrayList<>();
        if (positionedStacks == null) return stacks;
        for (PositionedStack positionedStack : positionedStacks) {
            if (positionedStack.items == null) continue;
            ItemStack[] items = positionedStack.items;
            stacks.add(items);
        }
        return stacks;
    }

    public static ArrayList<ItemStack[]> getItemStacks(PositionedStack positionedStacks) {
        ArrayList<ItemStack[]> list = new ArrayList<>();
        if (positionedStacks == null || positionedStacks.items == null) return list;
        list.add(positionedStacks.items);
        return list;
    }

    public static ArrayList<Object> getRecipeItems(ArrayList<ItemStack[]> itemStacks) {
        ArrayList<Object> list = new ArrayList<>();
        for (ItemStack[] stack : itemStacks) {
            if (stack == null) continue;
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
        Map<String, String[]> map = new LinkedHashMap<>();
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
                if (i >= idToStack.size()) continue;
                String name = idToName.get(i);
                ArrayList<ItemStack> items = idToStack.get(i);
                if (name == null || items == null || items.isEmpty()) continue;
                map.put(
                    name,
                    items.stream()
                        .map(Utils::getItemKey)
                        .toArray(String[]::new));
            }
        } catch (Exception e) {
            GTNHDumper.LOG.error(e);
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
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            String[] dictArray = entry.getValue();
            if (dictArray == null || dictArray.length != stackKeys.length) {
                continue;
            }
            Map<String, Long> dictFreq = Arrays.stream(dictArray)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

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
        return switch (x) {
            case 25 -> 1 + serialY;
            case 43 -> 2 + serialY;
            case 61 -> 3 + serialY;
            default -> -1;
        };
    }

    private static int getCraftingSerialY(int y) {
        return switch (y) {
            case 6 -> 0;
            case 24 -> 3;
            case 42 -> 6;
            default -> -1;
        };
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
        final boolean nameEqual = Item.itemRegistry.getNameForObject(a.getItem())
            .equals(Item.itemRegistry.getNameForObject(b.getItem()));
        final boolean metaEqual = InventoryUtils.actualDamage(a) == InventoryUtils.actualDamage(b);
        final boolean sizeEqual = a.stackSize == b.stackSize;
        final boolean nbtEqual = Objects.equals(a.stackTagCompound, b.stackTagCompound);
        return nameEqual && metaEqual && sizeEqual && nbtEqual;
    }

    public static boolean isItemEqual(ItemStack a, ItemStack b) {
        if (a == null) {
            return b == null;
        }
        final boolean nameEqual = a.getItem() == b.getItem();
        final boolean metaEqual = InventoryUtils.actualDamage(a) == InventoryUtils.actualDamage(b);
        return nameEqual && metaEqual;
    }

    public static boolean isStacksContain(ItemStack s, ItemStack[] stacks) {
        for (ItemStack stack : stacks) {
            if (isItemStackEqual(s, stack)) {
                return true;
            }
        }
        return false;
    }

    public static String replaceIllegalChars(String input) {
        return input.replace("/", "")
            .replace(":", "")
            .replace("*", "")
            .replace("\"", "")
            .replace("<", "")
            .replace(">", "")
            .replace("|", "")
            .replace("\\", "")
            .replace("?", "");
    }

    public static String replaceHuijiIllegalChars(String input) {
        return input.replace("/", "_")
            .replace(":", "_")
            .replace("*", "_")
            .replace("\"", "_")
            .replace("<", "_")
            .replace(">", "_")
            .replace("|", "_")
            .replace("\\", "_")
            .replace("?", "_");
    }

    public static String replacePathIllegalChars(String input) {
        return input.replace(":", "_")
            .replace("*", "_")
            .replace("\"", "_")
            .replace("<", "_")
            .replace(">", "_")
            .replace("|", "_")
            .replace("?", "_");
    }

    public static Gson getGsonInstance() {
        GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting()
            .serializeSpecialFloatingPointValues()
            .registerTypeAdapter(Double.class, new SafeDoubleSerializer())
            .registerTypeAdapter(Double.TYPE, new SafeDoubleSerializer())
            .disableHtmlEscaping()
            .registerTypeAdapter(RecipeItem.class, new RecipeItemSerializer())
            .registerTypeAdapter(ItemStack.class, new ItemStackSerializer())
            .registerTypeAdapter(FluidStack.class, new FluidStackSerializer())
            .registerTypeAdapter(NBTTagCompound.class, new NBTTagCompoundSerializer());
        if (CommonProxy.isGTLoaded) {
            gsonBuilder.registerTypeAdapter(Element.class, new ElementSerializer())
                .registerTypeAdapter(Materials.class, new MaterialsSerializer());
        }
        if (CommonProxy.isTCLoaded) {
            gsonBuilder.registerTypeAdapter(Aspect.class, new AspectSerializer())
                .registerTypeAdapter(AspectList.class, new AspectListSerializer());
        }
        return gsonBuilder.create();
    }

    public static String uuidToBase64(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        byte[] bytes = buffer.array();

        String base64 = Base64.getEncoder()
            .encodeToString(bytes);
        return base64.replaceAll("=", "");
    }

    public static String getItemStackShortKey(ItemStack stack) {
        String hash = hashNBT(stack);
        if (hash != null) {
            return getItemKey(stack) + "_" + hash;
        }
        return getItemKey(stack);

    }

    public static String hashNBT(ItemStack stack) {
        String nbt = getItemNBT(stack);
        if (nbt == null) return null;
        try {
            byte[] fullHash = MessageDigest.getInstance("SHA-256")
                .digest(nbt.getBytes(StandardCharsets.UTF_8));

            byte[] folded = new byte[16];
            for (int i = 0; i < 16; i++) {
                folded[i] = (byte) (fullHash[i] ^ fullHash[i + 16]);
            }

            BigInteger value = new BigInteger(1, folded);
            char[] buffer = new char[OUTPUT_LENGTH];
            for (int i = OUTPUT_LENGTH - 1; i >= 0; i--) {
                BigInteger[] div = value.divideAndRemainder(BASE);
                buffer[i] = BASE62[div[1].intValue()];
                value = div[0];
            }
            return new String(buffer);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
