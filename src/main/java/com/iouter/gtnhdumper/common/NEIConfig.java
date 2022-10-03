package com.iouter.gtnhdumper.common;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import com.iouter.gtnhdumper.GTNHDumper;

public class NEIConfig implements IConfigureNEI {
    @Override
    public void loadConfig() {
        API.addOption(new AdvItemPanelDumper());
    }

    @Override
    public String getName() {
        return GTNHDumper.MOD_ID;
    }

    @Override
    public String getVersion() {
        return GTNHDumper.VERSION;
    }
}
