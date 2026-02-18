package com.iouter.gtnhdumper.common.base;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import com.iouter.gtnhdumper.CommonProxy;
import com.iouter.gtnhdumper.GTNHDumper;
import com.iouter.gtnhdumper.common.dumper.AdvItemPanelDumper;
import com.iouter.gtnhdumper.common.dumper.FluidsDumper;
import com.iouter.gtnhdumper.common.dumper.GTMaterialDumper;
import com.iouter.gtnhdumper.common.dumper.GTNHDimensionDumper;
import com.iouter.gtnhdumper.common.dumper.GTOreVeinDumper;
import com.iouter.gtnhdumper.common.dumper.GTSmallOreVeinDumper;
import com.iouter.gtnhdumper.common.dumper.GTUndergroundFluidDumper;
import com.iouter.gtnhdumper.common.dumper.OreDictionaryDumper;
import com.iouter.gtnhdumper.common.dumper.RecipesDumper;
import com.iouter.gtnhdumper.common.dumper.TC4ResearchDumper;
import com.iouter.gtnhdumper.common.dumper.TICMaterialDumper;

public class NEIConfig implements IConfigureNEI {
    @Override
    public void loadConfig() {
        API.addOption(new AdvItemPanelDumper());
        API.addOption(new RecipesDumper());
        API.addOption(new OreDictionaryDumper());
        API.addOption(new FluidsDumper());
        if (CommonProxy.isGTLoaded) {
            API.addOption(new GTMaterialDumper());
            API.addOption(new GTOreVeinDumper());
            API.addOption(new GTSmallOreVeinDumper());
            API.addOption(new GTUndergroundFluidDumper());
            API.addOption(new GTNHDimensionDumper());
        }
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
