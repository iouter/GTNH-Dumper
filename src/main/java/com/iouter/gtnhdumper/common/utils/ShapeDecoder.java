package com.iouter.gtnhdumper.common.utils;

import com.gtnewhorizon.structurelib.structure.IStructureElement;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizon.structurelib.util.Vec3Impl;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ShapeDecoder {

    /**
     * 从 IStructureDefinition 实例解码形状。
     *
     * @param definition 结构定义实例
     * @param shapeName  形状名称
     * @return 原始 String[][] 形状，若不存在或解码失败返回空数组
     */
    public static String[][] decode(StructureDefinition<?> definition, String shapeName) {
        Map<String, String> shapes = definition.getShapes(); // 需要 getShapes() 方法
        String encoded = shapes.get(shapeName);
        if (encoded == null || encoded.isEmpty()) {
            return new String[0][0];
        }

        // 从 elements 中提取导航字符到位移向量的映射
        Map<Character, Vec3Impl> navigates = extractNavigates(definition.getElements());

        // 执行解码
        return decodeWithNavigates(encoded, navigates);
    }

    /**
     * 从 elements 映射中提取导航字符的位移信息。
     * 导航字符范围：\uD000 到 \uDFFF。
     */
    private static Map<Character, Vec3Impl> extractNavigates(Map<Character, ? extends IStructureElement<?>> elements) {
        Map<Character, Vec3Impl> result = new HashMap<>();
        for (Map.Entry<Character, ? extends IStructureElement<?>> entry : elements.entrySet()) {
            char ch = entry.getKey();
            if (ch >= '\uD000' && ch <= '\uDFFF') {
                Object elem = entry.getValue();
                // 尝试获取位移向量
                Vec3Impl vec = extractVecFromElement(elem);
                if (vec != null) {
                    result.put(ch, vec);
                }
            }
        }
        return result;
    }

    /**
     * 从 IStructureElement 实例中反射获取位移向量。
     * 期望该实例具有 getStepA(), getStepB(), getStepC(), resetA(), resetB() 方法。
     */
    private static Vec3Impl extractVecFromElement(Object elem) {
        try {
            Method getStepA = elem.getClass().getMethod("getStepA");
            Method getStepB = elem.getClass().getMethod("getStepB");
            Method getStepC = elem.getClass().getMethod("getStepC");
            int a = (int) getStepA.invoke(elem);
            int b = (int) getStepB.invoke(elem);
            int c = (int) getStepC.invoke(elem);
            return new Vec3Impl(a, b, c);
        } catch (Exception e) {
            // 不是导航元素，忽略
            return null;
        }
    }

    /**
     * 核心解码逻辑，使用明确的导航映射。
     */
    private static String[][] decodeWithNavigates(String encoded, Map<Character, Vec3Impl> navigates) {
        if (encoded == null || encoded.isEmpty()) {
            return new String[0][0];
        }

        // 存储每个单元格中的字符：cells[row][col][offset] = char
        Map<Integer, Map<Integer, Map<Integer, Character>>> cells = new HashMap<>();
        int row = 0, col = 0, offset = 0;

        for (char ch : encoded.toCharArray()) {
            Vec3Impl vec = navigates.get(ch);
            if (vec != null) {
                int da = vec.get0();
                int db = vec.get1();
                int dc = vec.get2();
                // 与 addShape 中 step 的逻辑一致
                boolean resetA = (dc > 0 || db > 0);
                boolean resetB = (dc > 0);

                offset += da;
                col += db;
                row += dc;
                if (resetA) offset = 0;
                if (resetB) col = 0;
            } else {
                // 普通字符
                cells.computeIfAbsent(row, r -> new HashMap<>())
                    .computeIfAbsent(col, c -> new HashMap<>())
                    .put(offset, ch);
                offset++;
            }
        }

        // 确定结果数组维度
        int maxRow = cells.keySet().stream().max(Integer::compareTo).orElse(-1);
        if (maxRow == -1) return new String[0][0];

        int maxCol = 0;
        for (Map<Integer, Map<Integer, Character>> rowMap : cells.values()) {
            int maxColInRow = rowMap.keySet().stream().max(Integer::compareTo).orElse(-1);
            if (maxColInRow > maxCol) maxCol = maxColInRow;
        }

        String[][] result = new String[maxRow + 1][maxCol + 1];
        for (int r = 0; r <= maxRow; r++) {
            Arrays.fill(result[r], "");
        }

        // 填充字符串
        for (Map.Entry<Integer, Map<Integer, Map<Integer, Character>>> rowEntry : cells.entrySet()) {
            int r = rowEntry.getKey();
            for (Map.Entry<Integer, Map<Integer, Character>> colEntry : rowEntry.getValue().entrySet()) {
                int c = colEntry.getKey();
                Map<Integer, Character> offsetMap = colEntry.getValue();
                int maxOffset = offsetMap.keySet().stream().max(Integer::compareTo).orElse(-1);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i <= maxOffset; i++) {
                    Character ch = offsetMap.get(i);
                    sb.append(ch != null ? ch : ' ');
                }
                result[r][c] = sb.toString();
            }
        }
        return result;
    }
}
