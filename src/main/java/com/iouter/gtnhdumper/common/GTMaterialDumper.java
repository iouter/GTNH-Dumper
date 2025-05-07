package com.iouter.gtnhdumper.common;

import codechicken.nei.config.DataDumper;
import bartworks.system.material.Werkstoff;
import cpw.mods.fml.common.Loader;
import gregtech.api.enums.Materials;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import net.minecraft.util.ChatComponentTranslation;

public class GTMaterialDumper extends DataDumper {

    public GTMaterialDumper() {
        super("tools.dump.gtnhdumper.gtmaterial");
    }

    @Override
    public String[] header() {
        return new String[] {
            "Material ID",
            "Material Default Name",
            "Material Localized Name",
            "Durability",
            "Tool Speed",
            "Tool Quality"
        };
    }

    @Override
    public Iterable<String[]> dump(int mode) {
        Materials[] gtMaterials = Materials.values();
        LinkedList<String[]> list = new LinkedList<>();
        for (Materials m : gtMaterials) {
            if (m != null) {
                list.add(new String[] {
                    String.valueOf(m.mMetaItemSubID),
                    m.mDefaultLocalName,
                    m.mLocalizedName,
                    String.valueOf(m.mDurability),
                    String.valueOf(m.mToolSpeed),
                    String.valueOf(m.mToolQuality)
                });
            }
        }

        if (Loader.isModLoaded("bartworks")) {
            LinkedHashSet<Werkstoff> bwMaterials = Werkstoff.werkstoffHashSet;
            for (Werkstoff m : bwMaterials) {
                if (m != null) {
                    list.add(new String[] {
                        String.valueOf(m.getmID()),
                        m.getDefaultName(),
                        m.getLocalizedName(),
                        String.valueOf(m.getStats().getDurOverride()),
                        String.valueOf(m.getStats().getSpeedOverride()),
                        String.valueOf(m.getStats().getQualityOverride())
                    });
                }
            }
        }

        return list;
    }

    @Override
    public ChatComponentTranslation dumpMessage(File file) {
        return new ChatComponentTranslation(
                "nei.options.tools.dump.gtnhdumper.gtmaterial.dumped", "dumps/" + file.getName());
    }

    @Override
    public int modeCount() {
        return 1;
    }
}
