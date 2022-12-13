package com.iouter.gtnhdumper.common;

import codechicken.lib.inventory.InventoryUtils;
import codechicken.nei.ItemPanels;
import codechicken.nei.config.ItemPanelDumper;
import codechicken.nei.guihook.GuiContainerManager;
import java.io.File;
import java.util.LinkedList;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;

public class AdvItemPanelDumper extends ItemPanelDumper {
    public static final Minecraft minecraft = Minecraft.getMinecraft();

    public AdvItemPanelDumper() {
        super("tools.dump.gtnhdumper.advitempanel");
    }

    @Override
    public String[] header() {
        return new String[] {"Item Name", "Item ID", "Item meta", "NBT", "Tooltip", "Display Name"};
    }

    @Override
    public Iterable<String[]> dump(int mode) {
        LinkedList<String[]> list = new LinkedList<>();
        for (ItemStack stack : ItemPanels.itemPanel.getItems()) {
            String Tooltip = getTooltip(stack);
            list.add(new String[] {
                Item.itemRegistry.getNameForObject(stack.getItem()),
                Integer.toString(Item.getIdFromItem(stack.getItem())),
                Integer.toString(InventoryUtils.actualDamage(stack)),
                stack.stackTagCompound == null ? "" : stack.stackTagCompound.toString(),
                Tooltip,
                EnumChatFormatting.getTextWithoutFormattingCodes(GuiContainerManager.itemDisplayNameShort(stack))
            });
        }
        return list;
    }

    public static String getTooltip(ItemStack itemStack) {
        String tooltip;
        try {
            tooltip = itemStack.getTooltip(minecraft.thePlayer, false).toString();
        } catch (Exception e) {
            tooltip = "ERROR";
        }
        return tooltip;
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
