package com.iouter.gtnhdumper.common.dumper;

import com.iouter.gtnhdumper.common.base.WikiDumper;
import gtneioreplugin.util.DimensionHelper;
import gtneioreplugin.util.GT5OreSmallHelper;
import gtneioreplugin.util.GT5UndergroundFluidHelper;
import net.minecraft.util.ChatComponentTranslation;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GTUndergroundFluidDumper extends WikiDumper {

    public GTUndergroundFluidDumper() {
        super("tools.dump.gtnhdumper.gtundergroundfluid");
    }

    @Override
    public String[] header() {
        return new String[] {
            "undergroundfluids"
        };
    }

    @Override
    public int getKeyIndex() {
        return 0;
    }

    @Override
    public String getKeyStr() {
        return "undergroundfluids";
    }

    @Override
    public Iterable<Object[]> dumpObject(int mode) {
        LinkedList<Object[]> list = new LinkedList<>();
        Map<String, List<GT5UndergroundFluidHelper.UndergroundFluidWrapper>> fluidMap = GT5UndergroundFluidHelper.getAllEntries();
        list.add(new Object[] {fluidMap});
        return list;
    }

    @Override
    public ChatComponentTranslation dumpMessage(File file) {
        return new ChatComponentTranslation(
            "nei.options.tools.dump.gtnhdumper.gtundergroundfluid.dumped", "dumps/" + file.getName());
    }

    @Override
    public int getMode() {
        return 2;
    }

    @Override
    public int modeCount() {
        return 1;
    }
}
