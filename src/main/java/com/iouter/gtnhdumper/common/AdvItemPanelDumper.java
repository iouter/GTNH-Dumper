package com.iouter.gtnhdumper.common;

import codechicken.nei.ItemPanels;
import codechicken.nei.config.ItemPanelDumper;
import codechicken.nei.guihook.GuiContainerManager;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.iouter.gtnhdumper.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

public class AdvItemPanelDumper extends ItemPanelDumper {
    public static final Minecraft minecraft = Minecraft.getMinecraft();

    public AdvItemPanelDumper() {
        super("tools.dump.gtnhdumper.advitempanel");
    }

    @Override
    public String[] header() {
        return new String[] {"key",
            "originalName",
            "translatedName",
            "tooltips"};
    }

    @Override
    public Iterable<String[]> dump(int mode) {
        LinkedList<String[]> list = new LinkedList<>();
        for (ItemStack stack : ItemPanels.itemPanel.getItems()) {
            list.add(new String[] {
                Utils.getItemKeyWithNBT(stack),
                EnumChatFormatting.getTextWithoutFormattingCodes(StatCollector.translateToFallback(stack.getUnlocalizedName() + ".name")),
                EnumChatFormatting.getTextWithoutFormattingCodes(GuiContainerManager.itemDisplayNameShort(stack)),
                getTooltip(stack)
            });
        }
        return list;
    }

    public static String getTooltip(ItemStack itemStack) {
        try {
            List<String> tooltips = itemStack.getTooltip(minecraft.thePlayer, false);
            return String.join("<br>", tooltips);
        } catch (Exception e) {
            return "ERROR";
        }
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
