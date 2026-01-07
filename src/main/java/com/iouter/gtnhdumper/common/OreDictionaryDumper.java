package com.iouter.gtnhdumper.common;

import com.iouter.gtnhdumper.Utils;
import com.iouter.gtnhdumper.common.base.WikiDumper;
import net.minecraft.util.ChatComponentTranslation;

import java.io.File;
import java.util.LinkedList;
import java.util.Map;

public class OreDictionaryDumper extends WikiDumper {
    public OreDictionaryDumper() {
        super("tools.dump.gtnhdumper.oreDictionary");
    }

    @Override
    public int getKeyIndex() {
        return 0;
    }

    @Override
    public String getKeyStr() {
        return "oreDictionaries";
    }

    @Override
    public String[] header() {
        return new String[]{"oreDict", "items"};
    }

    @Override
    public Iterable<Object[]> dumpObject(int mode) {
        LinkedList<Object[]> list = new LinkedList<>();
        Map<String, String[]> map = Utils.getOreDict();
        for (String key : map.keySet()) {
            list.add(new Object[]{
                key,
                map.get(key)
            });
        }
        return list;
    }

    @Override
    public ChatComponentTranslation dumpMessage(File file) {
        return new ChatComponentTranslation(
            "nei.options.tools.dump.gtnhdumper.oreDictionary.dumped", "dumps/" + file.getName());
    }
}
