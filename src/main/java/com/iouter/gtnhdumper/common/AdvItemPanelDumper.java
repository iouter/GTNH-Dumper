package com.iouter.gtnhdumper.common;

import codechicken.nei.guihook.GuiContainerManager;
import com.iouter.gtnhdumper.Utils;
import com.iouter.gtnhdumper.common.base.WikiDumper;
import com.iouter.gtnhdumper.common.utils.KeySimulator;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
        return "items";
    }

    @Override
    public String[] header() {
        return new String[] {
            "key",
            "nbt",
            "originalName",
            "translatedName",
            "tooltips",
            "tooltipsShift",
            "tooltipsCtrl",
            "tooltipsShiftAndCtrl",
            "mod"
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

            String nbt = Utils.getItemNBT(stack);
            String dumpNbt = nbt != null ? nbt : "";

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
            KeySimulator.withKeysPressed(() -> {
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
            list.add(new String[] {
                Utils.getItemKey(stack),
                dumpNbt,
                EnumChatFormatting.getTextWithoutFormattingCodes(StatCollector.translateToFallback(stack.getUnlocalizedName() + ".name")),
                EnumChatFormatting.getTextWithoutFormattingCodes(GuiContainerManager.itemDisplayNameShort(stack)),
                tooltip,
                tooltips[TOOLTIP_LSHIFT],
                tooltips[TOOLTIP_LCONTROL],
                tooltips[TOOLTIP_LSHIFT_AND_LCONTROL],
                modName
            });
        }
        return list;
    }

    @Override
    public ChatComponentTranslation dumpMessage(File file) {
        return new ChatComponentTranslation(
                "nei.options.tools.dump.gtnhdumper.advitempanel.dumped", "dumps/" + file.getName());
    }
}
