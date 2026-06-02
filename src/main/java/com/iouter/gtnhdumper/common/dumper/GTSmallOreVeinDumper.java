package com.iouter.gtnhdumper.common.dumper;

import java.io.File;
import java.util.LinkedList;

import net.minecraft.util.ChatComponentTranslation;

import com.iouter.gtnhdumper.common.base.WikiDumper;

import gtneioreplugin.util.GT5OreSmallHelper;

public class GTSmallOreVeinDumper extends WikiDumper {

    public GTSmallOreVeinDumper() {
        super("tools.dump.gtnhdumper.gtsmallorevein");
    }

    @Override
    public String[] header() {
        return new String[] { "key", "material", "amountPerChunk", "minY", "maxY", "dims" };
    }

    @Override
    public int getKeyIndex() {
        return 0;
    }

    @Override
    public String getKeyStr() {
        return "key";
    }

    @Override
    public Iterable<Object[]> dumpObject(int mode) {
        LinkedList<Object[]> list = new LinkedList<>();
        for (GT5OreSmallHelper.OreSmallWrapper vein : GT5OreSmallHelper.SMALL_ORES_BY_NAME.values()) {
            String[] heightRange = vein.worldGenHeightRange.split("-");
            list.add(
                new Object[] { vein.oreGenName, vein.material.getInternalName(), vein.amountPerChunk, heightRange[0],
                    heightRange[1], vein.enabledDims });
        }
        return list;
    }

    @Override
    public ChatComponentTranslation dumpMessage(File file) {
        return new ChatComponentTranslation(
            "nei.options.tools.dump.gtnhdumper.gtsmallorevein.dumped",
            "dumps/" + file.getName());
    }
}
