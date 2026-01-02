package com.iouter.gtnhdumper.common;

import com.iouter.gtnhdumper.CommonProxy;
import com.iouter.gtnhdumper.common.base.WikiDumper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategoryList;
import thaumcraft.api.research.ResearchItem;
import tuhljin.automagy.config.ModResearchItems;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class TC4ResearchDumper extends WikiDumper {
    public TC4ResearchDumper() {
        super("tools.dump.gtnhdumper.tc4research");
    }

    @Override
    public int getKeyIndex() {
        return 1;
    }

    @Override
    public String getKeyStr() {
        return "tc4Researches";
    }

    @Override
    public String[] header() {
        return new String[] {
            "Name",
            "Key",
            "Category",
            "Complexity",
            "Warp",
            "Parents",
            "ParentsHidden",
            "ItemTriggers",
            "EntityTriggers",
            "AspectTriggers",
            "KillerTriggers"
        };
    }

    @Override
    public Iterable<String[]> dump(int mode) {
        LinkedList<String[]> list = new LinkedList<>();
        for (ResearchCategoryList r : ResearchCategories.researchCategories.values()) {
            for (ResearchItem m : r.research.values()) {
                list.add(new String[] {
                    m.getName(),
                    m.key,
                    ResearchCategories.getCategoryName(m.category),
                    String.valueOf(m.getComplexity()),
                    String.valueOf(ThaumcraftApi.getWarp(m.key)),
                    getResearchsName(m.parents),
                    getResearchsName(m.parentsHidden),
                    getItemStacksName(m.getItemTriggers()),
                    getEnTitiesName(m.getEntityTriggers()),
                    getAspectsName(m.getAspectTriggers()),
                    getKillerTrigger(m)
                });
            }
        }
        return list;
    }

    @Override
    public ChatComponentTranslation dumpMessage(File file) {
        return new ChatComponentTranslation(
                "nei.options.tools.dump.gtnhdumper.tc4research.dumped", "dumps/" + file.getName());
    }

    public static String getResearchsName(String[] list) {
        if (list == null) return null;
        List<String> nameList = new ArrayList<>();
        for (String str : list) {
            nameList.add(ResearchCategories.getResearch(str).getName());
        }
        return String.join(ARRAY_SEPARATOR, nameList);
    }

    public static String getItemStacksName(ItemStack[] itemStacks) {
        if (itemStacks == null) return null;
        List<String> nameList = new ArrayList<>();
        for (ItemStack i : itemStacks) {
            nameList.add(i.getDisplayName());
        }
        return String.join(ARRAY_SEPARATOR, nameList);
    }

    public static String getAspectsName(Aspect[] aspects) {
        if (aspects == null) return null;
        List<String> nameList = new ArrayList<>();
        for (Aspect a : aspects) {

            nameList.add(a.getName());
        }
        return String.join(ARRAY_SEPARATOR, nameList);
    }

    public static String getEnTitiesName(String[] list) {
        if (list == null) return null;
        List<String> nameList = new ArrayList<>();
        for (String str : list) {
            nameList.add(StatCollector.translateToLocal("entity." + str + ".name"));
        }
        return String.join(ARRAY_SEPARATOR, nameList);
    }

    public String getKillerTrigger(ResearchItem researchItem) {
        if (CommonProxy.isAutomagyLoaded) {
            if (researchItem.category.equals("AUTOMAGY")) {
                Set<String> nameList = getKeysByValue(ModResearchItems.cluesOnKill, researchItem.key);
                String[] list = nameList.toArray(new String[0]);
                return String.join(ARRAY_SEPARATOR, list);
            }
        }
        return null;
    }

    public static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
        return map.entrySet().stream()
                .filter(entry -> Objects.equals(entry.getValue(), value))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}
