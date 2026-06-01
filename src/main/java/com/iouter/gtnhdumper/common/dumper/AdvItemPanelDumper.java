package com.iouter.gtnhdumper.common.dumper;

import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.shadow.org.apache.commons.csv.CSVFormat;
import codechicken.nei.shadow.org.apache.commons.csv.CSVPrinter;
import com.iouter.gtnhdumper.CommonProxy;
import com.iouter.gtnhdumper.common.base.WikiDumper;
import com.iouter.gtnhdumper.common.utils.KeySimulator;
import com.iouter.gtnhdumper.common.utils.Utils;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import net.glease.tc4tweak.modules.objectTag.GetObjectTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdvItemPanelDumper extends WikiDumper {

    private static final int TOOLTIP_LSHIFT = 0;
    private static final int TOOLTIP_LCONTROL = 1;
    private static final int TOOLTIP_LSHIFT_AND_LCONTROL = 2;

    public AdvItemPanelDumper() {
        super("tools.dump.gtnhdumper.advitempanel");
    }

    @Override
    public int getKeyIndex() {
        return 0;
    }

    @Override
    public String getKeyStr() {
        return "shortKey";
    }

    @Override
    public String[] header() {
        return new String[] {
            "shortKey",
            "key",
            "nbt",
            "originalName",
            "translatedName",
            "tooltips",
            "tooltipsShift",
            "tooltipsCtrl",
            "tooltipsShiftAndCtrl",
            "mod",
            "icon",
            "frameCount",
            "aspect"
        };
    }

    @Override
    public Iterable<Object[]> dumpObject(int mode) {
        LinkedList<Object[]> list = new LinkedList<>();

        List<ItemStack> itemStacks = new ArrayList<>();

        for (Object temp : GameData.getItemRegistry()) {
            if (!(temp instanceof Item)) continue;
            Item item = (Item) temp;
            List<ItemStack> sub = new ArrayList<>();
            item.getSubItems(item, CreativeTabs.tabAllSearch, sub);
            itemStacks.addAll(sub);
        }

        Map<String, String> redirectMap = new LinkedHashMap<>();
        Set<String> itemNameSet = new HashSet<>();

        Map<ItemStack, String> originalNameMap = getOriginalNameMap(itemStacks);

        for (ItemStack stack : itemStacks) {
            if (stack == null)
                continue;
            GameRegistry.UniqueIdentifier uid;
            try {
                uid = GameRegistry.findUniqueIdentifierFor(stack.getItem());
            } catch (Exception e) {
                continue;
            }
            if (uid == null)
                continue;
            String modid = uid.modId;
            ModContainer mod = Loader.instance().getIndexedModList().get(modid);

            ItemIconDumper.prepareRenderItem(stack, RenderItem.getInstance());

            String nbt = Utils.getItemNBT(stack);

            String modName = mod != null ? mod.getName() : modid;

            String tooltip = Utils.getTooltip(stack);
            String[] tooltips = new String[]{null, null, null};
            KeySimulator.withKeyPressed(() -> {
                String tooltipShift = Utils.getTooltip(stack);
                if (!tooltip.equals(tooltipShift)) {
                    tooltips[TOOLTIP_LSHIFT] = tooltipShift;
                }
            }, Keyboard.KEY_LSHIFT);
            KeySimulator.withKeyPressed(() -> {
                String tooltipCtrl = Utils.getTooltip(stack);
                String tooltipShift = tooltips[TOOLTIP_LSHIFT];
                if (tooltipShift == null) {
                    tooltipShift = tooltip;
                }
                if (!tooltip.equals(tooltipCtrl) && !tooltipShift.equals(tooltipCtrl)) {
                    tooltips[TOOLTIP_LCONTROL] = tooltipCtrl;
                }
            }, Keyboard.KEY_LCONTROL);
            KeySimulator.withKeyPressed(() -> {
                String tooltipShiftAndCtrl = Utils.getTooltip(stack);
                String tooltipShift = tooltips[TOOLTIP_LSHIFT];
                String tooltipCtrl = tooltips[TOOLTIP_LCONTROL];
                if (tooltipShift == null) {
                    tooltipShift = tooltip;
                }
                if (tooltipCtrl == null) {
                    tooltipCtrl = tooltip;
                }
                if (!tooltipShiftAndCtrl.equals(tooltipShift) && !tooltipShiftAndCtrl.equals(tooltipCtrl) && !tooltipShiftAndCtrl.equals(tooltip)) {
                    tooltips[TOOLTIP_LSHIFT_AND_LCONTROL] = tooltipShiftAndCtrl;
                }
            }, Keyboard.KEY_LSHIFT, Keyboard.KEY_LCONTROL);

            final String translatedName = EnumChatFormatting.getTextWithoutFormattingCodes(GuiContainerManager.itemDisplayNameShort(stack));
            final String imageName = ItemIconDumper.getIconFileName(stack);
            final String vaildFileName = Utils.replaceIllegalChars(translatedName);
            if (itemNameSet.add(vaildFileName)) {
                redirectMap.put("File:" + vaildFileName + ".png", "File:" + imageName);
            } else {
                int i = 2;
                while (!itemNameSet.add(vaildFileName + " " + i)) {
                    i++;
                }
                redirectMap.put("File:" + vaildFileName + " " + i + ".png", "File:" + imageName);
            }

            list.add(new Object[] {
                Utils.getItemStackShortKey(stack),
                Utils.getItemKey(stack),
                nbt,
                originalNameMap.get(stack),
                translatedName,
                tooltip,
                tooltips[TOOLTIP_LSHIFT],
                tooltips[TOOLTIP_LCONTROL],
                tooltips[TOOLTIP_LSHIFT_AND_LCONTROL],
                modName,
                imageName,
                ItemIconDumper.getItemFrameCount(stack),
                CommonProxy.isTCLoaded ? GetObjectTags.getObjectTags(stack) : null
            });
        }

        try (CSVPrinter printer = new CSVPrinter(Files.newBufferedWriter(Paths.get("dumps/image_redirect.csv")), CSVFormat.DEFAULT)) {
            for (Map.Entry<String, String> e : redirectMap.entrySet()) {
                printer.printRecord(e.getKey(), e.getValue());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    @Override
    public ChatComponentTranslation dumpMessage(File file) {
        return new ChatComponentTranslation(
                "nei.options.tools.dump.gtnhdumper.advitempanel.dumped", "dumps/" + file.getName());
    }

    private static Map<ItemStack, String> getOriginalNameMap(List<ItemStack> itemStacks){
        Minecraft minecraft = Minecraft.getMinecraft();
        LanguageManager languageManager = minecraft.getLanguageManager();
        Language currentLanguage = languageManager.getCurrentLanguage();
        languageManager.setCurrentLanguage(new Language("en_US", "US", "English (United States)", false));
        languageManager.onResourceManagerReload(minecraft.getResourceManager());

        Map<ItemStack, String> originalNameMap = new HashMap<>(itemStacks.size());
        for (ItemStack stack : itemStacks) {
            originalNameMap.put(stack, stack.getDisplayName());
        }
        languageManager.setCurrentLanguage(currentLanguage);
        languageManager.onResourceManagerReload(minecraft.getResourceManager());

        return originalNameMap;
    }
}
