package com.iouter.gtnhdumper.common.dumper;

import codechicken.lib.vec.Rectangle4i;
import codechicken.nei.NEIClientUtils;
import codechicken.nei.guihook.GuiContainerManager;
import com.iouter.gtnhdumper.Utils;
import com.iouter.gtnhdumper.common.base.WikiDumper;
import com.iouter.gtnhdumper.common.utils.FBOHelper;
import com.iouter.gtnhdumper.common.utils.KeySimulator;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AdvItemPanelDumper extends WikiDumper {

    private static final int TOOLTIP_LSHIFT = 0;
    private static final int TOOLTIP_LCONTROL = 1;
    private static final int TOOLTIP_LSHIFT_AND_LCONTROL = 2;

    private static final int[] resolutions = new int[]{16, 32, 48, 64, 128, 256};

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
            "mod",
            "icon"
        };
    }

    public int getRes() {
        int i = renderTag(name + ".res").getIntValue(0);
        if (i >= resolutions.length || i < 0) renderTag().setIntValue(i = 0);
        return resolutions[i];
    }

    public Rectangle4i resButtonSize() {
        int width = 50;
        return new Rectangle4i(modeButtonSize().x - width - 6, 0, width, 20);
    }

    @Override
    public void draw(int mousex, int mousey, float frame) {
        super.draw(mousex, mousey, frame);
        int res = getRes();
        drawButton(mousex, mousey, resButtonSize(), res + "x" + res);
    }

    @Override
    public void mouseClicked(int mousex, int mousey, int button) {
        if (resButtonSize().contains(mousex, mousey)) {
            NEIClientUtils.playClickSound();
            getTag(name + ".res").setIntValue((renderTag(name + ".res").getIntValue(0) + 1) % resolutions.length);
        } else super.mouseClicked(mousex, mousey, button);
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

        FBOHelper fbo = new FBOHelper(getRes());

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

//            ItemIconDumper.renderItem(stack, fbo, RenderItem.getInstance());

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
            list.add(new String[] {
                Utils.getItemKey(stack),
                nbt,
                originalNameMap.get(stack),
                EnumChatFormatting.getTextWithoutFormattingCodes(GuiContainerManager.itemDisplayNameShort(stack)),
                tooltip,
                tooltips[TOOLTIP_LSHIFT],
                tooltips[TOOLTIP_LCONTROL],
                tooltips[TOOLTIP_LSHIFT_AND_LCONTROL],
                modName,
                ItemIconDumper.getIconFileName(stack)
            });
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
