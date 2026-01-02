package com.iouter.gtnhdumper.common.base;

import codechicken.nei.config.DataDumper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

    private static Map<String, Map<String, Object>> getMapJson(String[] header, Iterable<String[]> contents, int keyIndex) {
        Map<String, Map<String, Object>> map = new LinkedHashMap<>();
        for (String[] content : contents) {
            map.put(content[keyIndex], getJsonMap(header, content));
        }
        return map;
    }

    private static Map<String, List<Map<String, Object>>> getListJson(String[] header, Iterable<String[]> contents, String keyStr) {
        Map<String, List<Map<String, Object>>> map = new LinkedHashMap<>();
        List<Map<String, Object>> list = new LinkedList<>();
        for (String[] content : contents) {
            list.add(getJsonMap(header, content));
        }
        map.put(keyStr, list);
        return map;
    }

    private static Map<String, Object> getJsonMap(String[] header, String[] content) {
        Map<String, Object> map= new LinkedHashMap<>(header.length);
        for (int i = 0; i < header.length; i++) {
            map.put(header[i], conventString(content[i]));
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
            double temp = Double.parseDouble(str);
            if (Double.isInfinite(temp))
                return "Infinite";
            if (Double.isNaN(temp))
                return "NaN";
            return temp;
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
    public void dumpTo(File file) throws IOException {
        Object dumpObject;
        int mode = getMode();
        if (mode == 0) {
            dumpObject = getListJson(header(), dump(0), getKeyStr());
        } else {
            dumpObject = getMapJson(header(), dump(mode), getKeyIndex());
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(dumpObject, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
