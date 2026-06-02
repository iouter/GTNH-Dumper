package com.iouter.gtnhdumper.common.utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;
import com.gtnewhorizon.structurelib.structure.IStructureElement;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizon.structurelib.util.Vec3Impl;

import blockrenderer6343.api.utils.CreativeItemSource;
import blockrenderer6343.client.utils.BRUtil;
import blockrenderer6343.client.world.DummyWorld;
import blockrenderer6343.integration.nei.StructureHacks;
import cpw.mods.fml.relauncher.ReflectionHelper;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.util.GTStructureUtility;
import gregtech.api.util.HatchElementBuilder;

public class StructureDumpHelper {

    // Copied from BlockRender6343
    private static final Field occupiedSpacesField;
    private static final MethodHandle DOT_GETTER, HINT_FALLBACK;
    private static final String HATCH_ELEMENT;

    static {
        HatchElementBuilder<?> builder = GTStructureUtility.buildHatchAdder()
            .adder((a, b, c) -> true)
            .casingIndex(1)
            .hint(1);
        IStructureElement<?> hatchEle = builder.hatchItemFilter((a, b) -> c -> true)
            .build();
        HATCH_ELEMENT = hatchEle.getClass()
            .getName();

        try {
            occupiedSpacesField = StructureDefinition.class.getDeclaredField("occupiedSpaces");
            occupiedSpacesField.setAccessible(true);
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle hatchBuilder = lookup
                .unreflectGetter(ReflectionHelper.findField(hatchEle.getClass(), "this$0"));
            MethodHandle hint = lookup.unreflectGetter(ReflectionHelper.findField(HatchElementBuilder.class, "mHint"));
            DOT_GETTER = MethodHandles.filterReturnValue(hatchBuilder, hint);
            HINT_FALLBACK = lookup
                .unreflect(ReflectionHelper.findMethod(hatchEle.getClass(), null, new String[] { "getHint" }));
        } catch (Exception e) {
            throw new RuntimeException("无法通过反射获取 occupiedSpaces 字段，请检查 StructureDefinition 源码是否变更", e);
        }
    }

    /**
     * 将 StructureDefinition 中压缩的 shapes 解码回原始的 String[][] 格式，并精准还原 '~' (Controller 标记)。
     * <p>
     * 原理：根据 addShape 源码，'~' 和普通空格 ' ' 在编译后都会变成步进(skip)操作并从字符串中抹除。
     * 但 '~' 会被额外记录在私有的 occupiedSpaces 字段中，而普通空格不会。
     * 本方法通过反射读取 occupiedSpaces，利用集合差集精准还原 '~' 的位置。
     *
     * @param structureDefinition 已构建的 StructureDefinition 实例
     * @return 解码后的 Map，Key 为结构名称，Value 为还原的 String[][] (维度为 [slice=c][row=b]，内部 String 长度为 a)
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String[][]> decodeShapes(StructureDefinition<?> structureDefinition) {
        Map<String, String[][]> result = new HashMap<>();
        Map<String, String> shapes = structureDefinition.getShapes();
        Map<String, IStructureElement<?>[]> structures = Collections
            .unmodifiableMap(structureDefinition.getStructures());

        // 1. 通过反射获取私有的 occupiedSpaces 字段
        Map<String, Set<Vec3Impl>> occupiedSpaces;
        try {
            occupiedSpaces = (Map<String, Set<Vec3Impl>>) occupiedSpacesField.get(structureDefinition);
        } catch (Exception e) {
            throw new RuntimeException("无法通过反射获取 occupiedSpaces 字段，请检查 StructureDefinition 源码是否变更", e);
        }

        for (Map.Entry<String, String> entry : shapes.entrySet()) {
            String name = entry.getKey();
            String shapeStr = entry.getValue();
            IStructureElement<?>[] elements = structures.get(name);
            Set<Vec3Impl> occupiedSpace = occupiedSpaces.getOrDefault(name, java.util.Collections.emptySet());

            if (elements == null || elements.length != shapeStr.length()) {
                throw new IllegalStateException("结构名称 '" + name + "' 的 shape 字符串与 elements 数组长度不匹配！");
            }

            // 记录实际放置的方块字符
            Map<String, Character> grid = getStringCharacterMap(shapeStr, elements);

            // 2. 确定最终网格的最大维度
            // (必须结合 occupiedSpace，以防结构只有 '~' 而没有其他方块，导致 grid 为空)
            int maxA = 0, maxB = 0, maxC = 0;
            for (String key : grid.keySet()) {
                String[] parts = key.split(",");
                maxC = Math.max(maxC, Integer.parseInt(parts[0]));
                maxB = Math.max(maxB, Integer.parseInt(parts[1]));
                maxA = Math.max(maxA, Integer.parseInt(parts[2]));
            }
            for (Vec3Impl vec : occupiedSpace) {
                maxA = Math.max(maxA, vec.get0()); // get0() 对应 a 轴 (aa)
                maxB = Math.max(maxB, vec.get1()); // get1() 对应 b 轴 (bb)
                maxC = Math.max(maxC, vec.get2()); // get2() 对应 c 轴 (cc)
            }

            // 3. 构建解码后的 String[][] (维度为 [slice=c][row=b])
            String[][] decoded = new String[maxC + 1][maxB + 1];
            for (int i = 0; i <= maxC; i++) {
                for (int j = 0; j <= maxB; j++) {
                    char[] row = new char[maxA + 1];
                    Arrays.fill(row, ' '); // 默认未占用的空间填充为空格

                    for (int k = 0; k <= maxA; k++) {
                        String key = i + "," + j + "," + k;
                        Character placed = grid.get(key);
                        if (placed != null) {
                            row[k] = placed;
                        } else if (occupiedSpace.contains(Vec3Impl.getFromPool(k, j, i))) {
                            // 核心逻辑：如果该位置没有实际方块字符，但却在 occupiedSpaces 中，
                            // 那么根据 addShape 的源码逻辑，它 100% 是原始的 '~' (Controller 标记)！
                            row[k] = '~';
                        }
                    }
                    decoded[i][j] = new String(row);
                }
            }

            result.put(name, decoded);
        }

        return result;
    }

    private static @NotNull Map<String, Character> getStringCharacterMap(String shapeStr,
        IStructureElement<?>[] elements) {
        Map<String, Character> grid = new HashMap<>();

        // 模拟 StructureUtility.iterateV2 的坐标追踪
        int a = 0, b = 0, c = 0;
        for (int i = 0; i < shapeStr.length(); i++) {
            char ch = shapeStr.charAt(i);
            IStructureElement<?> elem = elements[i];

            if (elem.isNavigating()) {
                a = (elem.resetA() ? 0 : a) + elem.getStepA();
                b = (elem.resetB() ? 0 : b) + elem.getStepB();
                c = (elem.resetC() ? 0 : c) + elem.getStepC();
            } else {
                grid.put(c + "," + b + "," + a, ch);
                a++;
            }
        }
        return grid;
    }

    // Copied from BlockRender6343
    public static int getDotForElement(IMetaTileEntity te, IStructureElement<IMetaTileEntity> element) {
        IStructureElement<?> match = StructureHacks.getFirstMatchingElement(HATCH_ELEMENT, te, element);
        if (match == null) return -1;

        try {
            return (int) DOT_GETTER.invokeWithArguments(match);
        } catch (Throwable e) {
            return -1;
        }
    }

    public static Object scanCandidates(Object multi, IStructureElement<Object> element, ItemStack buildStack,
        BlockPos block) {
        if (element == null) return Collections.emptyList();
        IStructureElement.BlocksToPlace blocksToPlace = element.getBlocksToPlace(
            multi,
            DummyWorld.INSTANCE,
            block.x,
            block.y,
            block.z,
            buildStack,
            BRUtil.AUTO_PLACE_ENVIRONMENT);
        if (blocksToPlace == null) return Collections.emptyList();

        Set<ItemStack> rawCandidates = CreativeItemSource.instance
            .takeEverythingMatches(blocksToPlace.getPredicate(), false, 0)
            .keySet();
        if (rawCandidates.size() > 1000) {
            return rawCandidates;
        }
        List<List<ItemStack>> stackedCandidates = new ArrayList<>();
        for (ItemStack rawCandidate : rawCandidates) {
            boolean added = false;
            for (List<ItemStack> stackedCandidate : stackedCandidates) {
                List<String> firstCandidateTooltip = stackedCandidate.get(0)
                    .getTooltip(BRUtil.FAKE_PLAYER, false);
                List<String> rawCandidateTooltip = rawCandidate.getTooltip(BRUtil.FAKE_PLAYER, false);
                if (firstCandidateTooltip.size() > 1 && rawCandidateTooltip.size() > 1
                    && firstCandidateTooltip.get(1)
                        .equals(rawCandidateTooltip.get(1))) {
                    stackedCandidate.add(rawCandidate);
                    added = true;
                    break;
                }
            }
            if (!added) {
                List<ItemStack> newStackedCandidate = new ArrayList<>();
                newStackedCandidate.add(rawCandidate);
                stackedCandidates.add(newStackedCandidate);
            }
        }

        return stackedCandidates;
    }
}
