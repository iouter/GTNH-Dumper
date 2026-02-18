package com.iouter.gtnhdumper.common.base;

import codechicken.nei.config.DataDumper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iouter.gtnhdumper.CommonProxy;
import com.iouter.gtnhdumper.common.recipe.base.RecipeItem;
import com.iouter.gtnhdumper.common.recipe.serializer.AspectListSerializer;
import com.iouter.gtnhdumper.common.recipe.serializer.AspectSerializer;
import com.iouter.gtnhdumper.common.recipe.serializer.ElementSerializer;
import com.iouter.gtnhdumper.common.recipe.serializer.FluidStackSerializer;
import com.iouter.gtnhdumper.common.recipe.serializer.ItemStackSerializer;
import com.iouter.gtnhdumper.common.recipe.serializer.MaterialsSerializer;
import com.iouter.gtnhdumper.common.recipe.serializer.RecipeItemSerializer;
import com.iouter.gtnhdumper.common.recipe.serializer.SafeDoubleSerializer;
import gregtech.api.enums.Element;
import gregtech.api.enums.Materials;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class WikiDumper extends DataDumper{

    public static String ARRAY_SEPARATOR = ";;;";

    public WikiDumper(String name) {
        super(name);
    }

    private static Map<String, Map<String, Object>> getMapJson(String[] header, Iterable<Object[]> contents, int keyIndex) {
        Map<String, Map<String, Object>> map = new LinkedHashMap<>();
        for (Object[] content : contents) {
            map.put(content[keyIndex].toString(), getJsonMap(header, content));
        }
        return map;
    }

    private static Map<String, List<Map<String, Object>>> getListJson(String[] header, Iterable<Object[]> contents, String keyStr) {
        Map<String, List<Map<String, Object>>> map = new LinkedHashMap<>();
        List<Map<String, Object>> list = new LinkedList<>();
        for (Object[] content : contents) {
            list.add(getJsonMap(header, content));
        }
        map.put(keyStr, list);
        return map;
    }

    private static Map<String, Object> getJsonMap(String[] header, Object[] content) {
        Map<String, Object> map= new LinkedHashMap<>(header.length);
        for (int i = 0; i < header.length; i++) {
            Object temp = content[i];
            if (temp instanceof String)
                map.put(header[i], conventString((String) temp));
            else
                map.put(header[i], temp);
        }
        return map;
    }

    private static Object conventString(String str) {
        if (str == null)
            return null;
        str = str.trim();
        if (str.isEmpty() || str.equals("null"))
            return null;
        if (str.contains(ARRAY_SEPARATOR))
            return Arrays.stream(str.split(ARRAY_SEPARATOR)).map(WikiDumper::conventString).toArray(Object[]::new);
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException ignored) {}
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException ignored) {}
        if ("true".equalsIgnoreCase(str)) {
            return true;
        } else if ("false".equalsIgnoreCase(str)) {
            return false;
        }
        return str;
    }

    public abstract int getKeyIndex();

    public abstract String getKeyStr();

    @Override
    public String getFileExtension() {
        return ".json";
    }

    @Override
    public int modeCount() {
        return 2;
    }

    @Override
    public String modeButtonText() {
        int mode = getMode();
        if (mode == 0) {
            return "List";
        } else {
            return "Map";
        }
    }

    @Override
    public Iterable<String[]> dump(int mode) {
        return null;
    }

    public abstract Iterable<Object[]> dumpObject(int mode);

    @Override
    public void dumpTo(File file) throws IOException {
        Object dumpObject;
        int mode = getMode();
        if (mode == 0) {
            dumpObject = getListJson(header(), dumpObject(0), getKeyStr());
        } else {
            dumpObject = getMapJson(header(), dumpObject(mode), getKeyIndex());
        }
        GsonBuilder gsonBuilder = new GsonBuilder()
            .setPrettyPrinting()
            .serializeSpecialFloatingPointValues()
            .registerTypeAdapter(Double.class, new SafeDoubleSerializer())
            .registerTypeAdapter(Double.TYPE, new SafeDoubleSerializer())
            .disableHtmlEscaping()
            .registerTypeAdapter(RecipeItem.class, new RecipeItemSerializer())
            .registerTypeAdapter(ItemStack.class, new ItemStackSerializer())
            .registerTypeAdapter(FluidStack.class, new FluidStackSerializer());
        if (CommonProxy.isGTLoaded) {
            gsonBuilder.registerTypeAdapter(Element.class, new ElementSerializer())
                .registerTypeAdapter(Materials.class, new MaterialsSerializer());
        }
        if (CommonProxy.isTCLoaded) {
            gsonBuilder.registerTypeAdapter(Aspect.class, new AspectSerializer())
                .registerTypeAdapter(AspectList.class, new AspectListSerializer());
        }
        Gson gson = gsonBuilder.create();
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(dumpObject, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
