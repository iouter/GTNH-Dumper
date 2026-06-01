package com.iouter.gtnhdumper.common.base;

import codechicken.nei.config.DataDumper;
import com.iouter.gtnhdumper.GTNHDumper;

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
        } else if (mode == 1){
            dumpObject = getMapJson(header(), dumpObject(1), getKeyIndex());
        } else {
            dumpObject = getJsonMap(header(), dumpObject(2).iterator().next());
        }
        try (FileWriter writer = new FileWriter(file)) {
            GTNHDumper.GSON.toJson(dumpObject, writer);
        } catch (IOException e) {
            GTNHDumper.LOG.error(e);
        }
    }
}
