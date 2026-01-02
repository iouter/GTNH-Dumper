package com.iouter.gtnhdumper.common;

import codechicken.nei.config.DataDumper;
import bartworks.system.material.Werkstoff;
import cpw.mods.fml.common.Loader;
import gregtech.api.enums.Materials;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import net.minecraft.util.ChatComponentTranslation;

public class GTOreVeinDumper extends DataDumper {

    public GTOreVeinDumper() {
        super("tools.dump.gtnhdumper.gtorevein");
    }

    @Override
    public String[] header() {
        return new String[] {
        };
    }

    @Override
    public Iterable<String[]> dump(int mode) {
        LinkedList<String[]> list = new LinkedList<>();
        return list;
    }

    @Override
    public ChatComponentTranslation dumpMessage(File file) {
        return new ChatComponentTranslation(
            "nei.options.tools.dump.gtnhdumper.gtorevein.dumped", "dumps/" + file.getName());
    }
}
