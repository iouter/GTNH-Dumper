package com.iouter.gtnhdumper.common.dumper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.glease.tc4tweak.modules.objectTag.GetObjectTags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.input.Keyboard;

import com.iouter.gtnhdumper.CommonProxy;
import com.iouter.gtnhdumper.GTNHDumper;
import com.iouter.gtnhdumper.common.base.WikiDumper;
import com.iouter.gtnhdumper.common.utils.KeySimulator;
import com.iouter.gtnhdumper.common.utils.Utils;

import codechicken.nei.guihook.GuiContainerManager;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import thaumcraft.api.aspects.AspectList;

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
        return new String[] { "shortKey", "key", "nbt", "originalName", "translatedName", "tooltips", "tooltipsShift",
            "tooltipsCtrl", "tooltipsShiftAndCtrl", "mod", "icon", "aspect" };
    }

    @Override
    public Iterable<Object[]> dumpObject(int mode) {
        LinkedList<Object[]> list = new LinkedList<>();

        List<ItemStack> itemStacks = new ArrayList<>();

        for (Object temp : GameData.getItemRegistry()) {
            if (!(temp instanceof Item item)) continue;
            List<ItemStack> sub = new ArrayList<>();
            item.getSubItems(item, CreativeTabs.tabAllSearch, sub);
            itemStacks.addAll(sub);
        }
        Map<ItemStack, String> originalNameMap = getOriginalNameMap(itemStacks);
        for (ItemStack stack : itemStacks) {
            if (stack == null) continue;
            GameRegistry.UniqueIdentifier uid;
            try {
                uid = GameRegistry.findUniqueIdentifierFor(stack.getItem());
            } catch (Exception e) {
                continue;
            }
            if (uid == null) continue;
            String modid = uid.modId;
            ModContainer mod = Loader.instance()
                .getIndexedModList()
                .get(modid);
            String nbt = Utils.getItemNBT(stack);
            String modName = mod != null ? mod.getName() : modid;
            String tooltip = Utils.getTooltip(stack);
            String[] tooltips = new String[] { null, null, null };
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
                if (!tooltipShiftAndCtrl.equals(tooltipShift) && !tooltipShiftAndCtrl.equals(tooltipCtrl)
                    && !tooltipShiftAndCtrl.equals(tooltip)) {
                    tooltips[TOOLTIP_LSHIFT_AND_LCONTROL] = tooltipShiftAndCtrl;
                }
            }, Keyboard.KEY_LSHIFT, Keyboard.KEY_LCONTROL);

            final String translatedName = EnumChatFormatting
                .getTextWithoutFormattingCodes(GuiContainerManager.itemDisplayNameShort(stack));
            final String imageName = ItemIconDumper.getIconFileName(stack);
            AspectList aspectList = null;
            if (CommonProxy.isTCLoaded) {
                try {
                    aspectList = GetObjectTags.getObjectTags(stack);
                } catch (Exception e) {
                    GTNHDumper.LOG.error(e);
                }
            }

            list.add(
                new Object[] { Utils.getItemStackShortKey(stack), Utils.getItemKey(stack), nbt,
                    originalNameMap.get(stack), translatedName, tooltip, tooltips[TOOLTIP_LSHIFT],
                    tooltips[TOOLTIP_LCONTROL], tooltips[TOOLTIP_LSHIFT_AND_LCONTROL], modName, imageName,
                    aspectList });
        }

        return list;
    }

    @Override
    public ChatComponentTranslation dumpMessage(File file) {
        return new ChatComponentTranslation(
            "nei.options.tools.dump.gtnhdumper.advitempanel.dumped",
            "dumps/" + file.getName());
    }

    private static Map<ItemStack, String> getOriginalNameMap(List<ItemStack> itemStacks) {
        Map<ItemStack, String> originalNameMap = new HashMap<>(itemStacks.size());
        Utils.getEnglishTranslation(() -> {
            for (ItemStack stack : itemStacks) {
                originalNameMap.put(stack, stack.getDisplayName());
            }
        });
        return originalNameMap;
    }
}
