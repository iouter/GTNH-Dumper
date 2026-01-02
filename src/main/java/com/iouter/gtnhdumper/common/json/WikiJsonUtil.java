package com.iouter.gtnhdumper.common.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WikiJsonUtil {
    private WikiJsonUtil() {}

    protected static Map<String, Map<String, Object>> getMapJson(String[] header, Iterable<String[]> contents, int keyIndex) {
        Map<String, Map<String, Object>> map = new LinkedHashMap<>();
        for (String[] content : contents) {
            map.put(content[keyIndex], getJsonMap(header, content));
        }
        return map;
    }

    protected static Map<String, List<Map<String, Object>>> getListJson(String[] header, Iterable<String[]> contents, String keyStr) {
        Map<String, List<Map<String, Object>>> map = new LinkedHashMap<>();
        List<Map<String, Object>> list = new LinkedList<>();
        for (String[] content : contents) {
            list.add(getJsonMap(header, content));
        }
        map.put(keyStr, list);
        return map;
    }

    protected static Map<String, Object> getJsonMap(String[] header, String[] content) {
        Map<String, Object> map= new LinkedHashMap<>(header.length);
        for (int i = 0; i < header.length; i++) {
            map.put(header[i], conventString(content[i]));
        }
        return map;
    }

    protected static Object conventString(String str) {
        if (str == null)
            return null;
        str = str.trim();
        if (str.isEmpty() || str.equals("null"))
            return null;
        if (str.contains(";;;"))
            return Arrays.stream(str.split(";;;")).map(WikiJsonUtil::conventString).toArray(Object[]::new);
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
}
