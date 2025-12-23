package com.iouter.gtnhdumper.common;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import com.iouter.gtnhdumper.CommonProxy;
import com.iouter.gtnhdumper.GTNHDumper;
import cpw.mods.fml.common.Loader;

public class NEIConfig implements IConfigureNEI {
    @Override
    public void loadConfig() {
        API.addOption(new AdvItemPanelDumper());
        API.addOption(new RecipesDumper());
        API.addOption(new OreDictionaryDumper());
        API.addOption(new FluidsDumper());
        if (CommonProxy.isGTLoaded) API.addOption(new GTMaterialDumper());
        if (CommonProxy.isTiCLoaded) API.addOption(new TICMaterialDumper());
        if (CommonProxy.isTCLoaded) API.addOption(new TC4ResearchDumper());
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
