package com.iouter.gtnhdumper.common.dumper;

import com.iouter.gtnhdumper.common.base.WikiDumper;
import gtneioreplugin.util.DimensionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class GTNHDimensionDumper extends WikiDumper {
    public GTNHDimensionDumper() {
        super("tools.dump.gtnhdumper.gtnhdimension");
    }

    @Override
    public int getKeyIndex() {
        return 0;
    }

    @Override
    public String getKeyStr() {
        return "abbreviatedName";
    }

    @Override
    public Iterable<Object[]> dumpObject(int mode) {
        LinkedList<Object[]> list = new LinkedList<>();

        String[] dimensionOriginalNames = new String[DimensionHelper.DimNameTrimmed.length];

        Minecraft minecraft = Minecraft.getMinecraft();
        LanguageManager languageManager = minecraft.getLanguageManager();
        Language currentLanguage = languageManager.getCurrentLanguage();
        languageManager.setCurrentLanguage(new Language("en_US", "US", "English (United States)", false));
        languageManager.onResourceManagerReload(minecraft.getResourceManager());

        for (int i = 0; i < DimensionHelper.DimNameTrimmed.length; i++) {
            dimensionOriginalNames[i] = DimensionHelper.getDimLocalizedName(DimensionHelper.DimNameTrimmed[i]);
        }

        languageManager.setCurrentLanguage(currentLanguage);
        languageManager.onResourceManagerReload(minecraft.getResourceManager());

        for (int i = 0; i < DimensionHelper.DimNameTrimmed.length; i++) {
            String dimNameTrimmed = DimensionHelper.DimNameTrimmed[i];
            String abbreviatedName = DimensionHelper.DimNameDisplayed[i];
            String fullName = DimensionHelper.getFullName(abbreviatedName);
            list.add(new Object[] {
                abbreviatedName,
                dimNameTrimmed,
                fullName,
                dimensionOriginalNames[i],
                DimensionHelper.getDimLocalizedName(dimNameTrimmed),
                DimensionHelper.getDimTier(fullName).replace("gtnop.tier.", "")
            });
        }
        return list;
    }

    @Override
    public String[] header() {
        return new String[] {
            "abbreviatedName",
            "internalName",
            "fullName",
            "originalName",
            "localizedName",
            "tier"
        };
    }

    @Override
    public ChatComponentTranslation dumpMessage(File file) {
        return new ChatComponentTranslation(
            "nei.options.tools.dump.gtnhdumper.gtnhdimension.dumped", "dumps/" + file.getName());
    }
}
