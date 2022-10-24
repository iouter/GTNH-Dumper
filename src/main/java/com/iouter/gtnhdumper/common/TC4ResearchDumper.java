package com.iouter.gtnhdumper.common;

import codechicken.nei.config.DataDumper;
import cpw.mods.fml.common.Loader;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategoryList;
import thaumcraft.api.research.ResearchItem;
import tuhljin.automagy.config.ModResearchItems;

public class TC4ResearchDumper extends DataDumper {
    public TC4ResearchDumper() {
        super("tools.dump.gtnhdumper.tc4research");
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
                    Arrays.toString(TC4RKeytoRName(m.parents)),
                    Arrays.toString(TC4RKeytoRName(m.parentsHidden)),
                    Arrays.toString(ItemStackstoName(m.getItemTriggers())),
                    Arrays.toString(EnTityToName(m.getEntityTriggers())),
                    Arrays.toString(AspectsToName(m.getAspectTriggers())),
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

    @Override
    public int modeCount() {
        return 1;
    }

    public static String[] TC4RKeytoRName(String[] list) {
        if (list == null) return null;
        List<String> nameList = new ArrayList<>();
        for (String str : list) {
            nameList.add(ResearchCategories.getResearch(str).getName());
        }
        return nameList.toArray(new String[0]);
    }

    public static String[] ItemStackstoName(ItemStack[] itemStacks) {
        if (itemStacks == null) return null;
        List<String> nameList = new ArrayList<>();
        for (ItemStack i : itemStacks) {
            nameList.add(i.getDisplayName());
        }
        return nameList.toArray(new String[0]);
    }

    public static String[] AspectsToName(Aspect[] aspects) {
        if (aspects == null) return null;
        List<String> nameList = new ArrayList<>();
        for (Aspect a : aspects) {
            nameList.add(a.getName());
        }
        return nameList.toArray(new String[0]);
    }

    public static String[] EnTityToName(String[] list) {
        if (list == null) return null;
        List<String> nameList = new ArrayList<>();
        for (String str : list) {
            nameList.add(StatCollector.translateToLocal("entity." + str + ".name"));
        }
        return nameList.toArray(new String[0]);
    }

    public String getKillerTrigger(ResearchItem researchItem) {
        if (Loader.isModLoaded("Automagy")) {
            if (researchItem.category.equals("AUTOMAGY")) {
                Set<String> nameList = getKeysByValue(ModResearchItems.cluesOnKill, researchItem.key);
                String[] list = nameList.toArray(new String[0]);
                return Arrays.toString(EnTityToName(list));
            }
        }
        return "null";
    }

    public static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
        return map.entrySet().stream()
                .filter(entry -> Objects.equals(entry.getValue(), value))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}
