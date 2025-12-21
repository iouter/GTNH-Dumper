package com.iouter.gtnhdumper.common;

import codechicken.nei.config.DataDumper;
import com.iouter.gtnhdumper.Utils;
import net.minecraft.util.ChatComponentTranslation;

import java.io.File;
import java.util.LinkedList;
import java.util.Map;

public class OreDictionaryDumper extends DataDumper {
    public OreDictionaryDumper() {
        super("tools.dump.gtnhdumper.oreDictionary");
    }

    @Override
    public String[] header() {
        return new String[]{"oreDict", "items"};
    }

    @Override
    public Iterable<String[]> dump(int mode) {
        LinkedList<String[]> list = new LinkedList<>();
        Map<String, String[]> map = Utils.getOreDict();
        for (String key : map.keySet()) {
            list.add(new String[]{
                key,
                String.join(", ", map.get(key))
            });
        }
        return list;
    }

    @Override
    public ChatComponentTranslation dumpMessage(File file) {
        return new ChatComponentTranslation(
            "nei.options.tools.dump.gtnhdumper.oreDictionary.dumped", "dumps/" + file.getName());
    }

    @Override
    public int modeCount() {
        return 1;
    }
}
