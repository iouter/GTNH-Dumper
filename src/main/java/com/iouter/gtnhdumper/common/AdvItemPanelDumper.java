package com.iouter.gtnhdumper.common;

import codechicken.lib.inventory.InventoryUtils;
import codechicken.nei.ItemPanels;
import codechicken.nei.config.ItemPanelDumper;
import codechicken.nei.guihook.GuiContainerManager;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;

public class AdvItemPanelDumper extends ItemPanelDumper {
    public AdvItemPanelDumper() {
        super("tools.dump.gtnhdumper.advitempanel");
    }

    public static String ListToString(List list) {
        String str = "";
        if (list != null) {
            str = str.concat(list.get(0) == null ? "" : list.get(0).toString());
        }
        if (list.size() > 1) {
            for (int i = 1; i < list.size(); i++) {
                str = str.concat("<br />");
                str = str.concat(list.get(i) == null ? "" : list.get(i).toString());
            }
        }
        return str;
    }

    @Override
    public String[] header() {
        return new String[] {"Item Name", "Item ID", "Item meta", "NBT", "Tooltip", "Display Name"};
    }

    @Override
    public Iterable<String[]> dump(int mode) {
        final Minecraft minecraft = Minecraft.getMinecraft();
        LinkedList<String[]> list = new LinkedList<>();
        for (ItemStack stack : ItemPanels.itemPanel.getItems()) {
            String Tooltip;
            try {
                Tooltip = ListToString(stack.getTooltip(minecraft.thePlayer, false));
            } catch (Exception e) {
                Tooltip = "Has ERROR";
            }
            list.add(new String[] {
                Item.itemRegistry.getNameForObject(stack.getItem()),
                Integer.toString(Item.getIdFromItem(stack.getItem())),
                Integer.toString(InventoryUtils.actualDamage(stack)),
                stack.stackTagCompound == null
                        ? ""
                        : stack.stackTagCompound.toString().replace(",", "*comma*"),
                Tooltip,
                EnumChatFormatting.getTextWithoutFormattingCodes(GuiContainerManager.itemDisplayNameShort(stack))
            });
        }
        return list;
    }

    @Override
    public ChatComponentTranslation dumpMessage(File file) {
        return new ChatComponentTranslation(
                "nei.options.tools.dump.gtnhdumper.advitempanel.dumped", "dumps/" + file.getName());
    }

    @Override
    public int modeCount() {
        return 1;
    }
}
