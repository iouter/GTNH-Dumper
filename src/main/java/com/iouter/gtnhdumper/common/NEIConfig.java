package com.iouter.gtnhdumper.common;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import com.iouter.gtnhdumper.GTNHDumper;
import cpw.mods.fml.common.Loader;

public class NEIConfig implements IConfigureNEI {
    @Override
    public void loadConfig() {
        API.addOption(new AdvItemPanelDumper());
        if (Loader.isModLoaded("gregtech")) API.addOption(new GTMaterialDumper());
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
