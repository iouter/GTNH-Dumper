package com.iouter.gtnhdumper.common;

import codechicken.nei.guihook.GuiContainerManager;
import com.iouter.gtnhdumper.Utils;
import com.iouter.gtnhdumper.common.base.WikiDumper;
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

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AdvItemPanelDumper extends WikiDumper {

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
        return new String[] {"key",
            "nbt",
            "originalName",
            "translatedName",
            "tooltips",
            "mod"};
    }

    @Override
    public Iterable<String[]> dump(int mode) {
        LinkedList<String[]> list = new LinkedList<>();

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
            GameRegistry.UniqueIdentifier uid = null;
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
            list.add(new String[] {
                Utils.getItemKey(stack),
                dumpNbt,
                EnumChatFormatting.getTextWithoutFormattingCodes(StatCollector.translateToFallback(stack.getUnlocalizedName() + ".name")),
                EnumChatFormatting.getTextWithoutFormattingCodes(GuiContainerManager.itemDisplayNameShort(stack)),
                Utils.getTooltip(stack),
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
