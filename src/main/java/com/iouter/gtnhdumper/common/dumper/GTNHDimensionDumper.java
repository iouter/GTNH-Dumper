package com.iouter.gtnhdumper.common.dumper;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import net.minecraft.util.ChatComponentTranslation;

import com.iouter.gtnhdumper.common.base.WikiDumper;
import com.iouter.gtnhdumper.common.utils.Utils;

import gtneioreplugin.util.DimensionHelper;

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

        Map<DimensionHelper.Dimension, String> originalNameMap = new HashMap<>();

        Utils.getEnglishTranslation(() -> {
            for (DimensionHelper.Dimension dimension : DimensionHelper.ALL_DIMENSIONS) {
                originalNameMap
                    .put(dimension, DimensionHelper.getDimLocalizedName(DimensionHelper.getFullName(dimension.abbr())));
            }
        });

        for (DimensionHelper.Dimension dimension : DimensionHelper.ALL_DIMENSIONS) {
            list.add(
                new Object[] { dimension.abbr(), dimension.internalName(), dimension.fullName(),
                    originalNameMap.get(dimension),
                    DimensionHelper.getDimLocalizedName(DimensionHelper.getFullName(dimension.abbr())),
                    dimension.tierKey()
                        .replace("gtnop.tier.", "") });
        }
        return list;
    }

    @Override
    public String[] header() {
        return new String[] { "abbreviatedName", "internalName", "fullName", "originalName", "localizedName", "tier" };
    }

    @Override
    public ChatComponentTranslation dumpMessage(File file) {
        return new ChatComponentTranslation(
            "nei.options.tools.dump.gtnhdumper.gtnhdimension.dumped",
            "dumps/" + file.getName());
    }
}
