package com.iouter.gtnhdumper.common;

import codechicken.nei.config.DataDumper;
import net.minecraft.util.ChatComponentTranslation;

import java.io.File;
import java.util.LinkedList;

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
