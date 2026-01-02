package com.iouter.gtnhdumper.common;

import codechicken.nei.config.DataDumper;
import com.iouter.gtnhdumper.Utils;
import com.iouter.gtnhdumper.common.json.WikiJsonInterface;
import net.minecraft.util.ChatComponentTranslation;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;

public class OreDictionaryDumper extends DataDumper implements WikiJsonInterface {
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
    public String getFileExtension() {
        return getFileExtensionWiki();
    }

    @Override
    public int modeCount() {
        return modeCountWiki();
    }

    @Override
    public String modeButtonText() {
        return modeButtonTextWiki();
    }

    @Override
    public void dumpTo(File file) throws IOException {
        dumpToWiki(file);
    }
}
