package com.iouter.gtnhdumper.common;

import bartworks.system.material.Werkstoff;
import codechicken.nei.config.DataDumper;
import gregtech.api.enums.Materials;
import net.minecraft.util.ChatComponentTranslation;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.LinkedList;

public class GTMaterialDumper extends DataDumper {

    public GTMaterialDumper() {
        super("tools.dump.gtnhdumper.gtmaterial");
    }

    @Override
    public String[] header() {
        return new String[]{
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
        //gt5
        Materials[] gtMaterials = Materials.values();
        LinkedList<String[]> list = new LinkedList<>();
        for (Materials m : gtMaterials) {
            if (m != null) {
                dumpMaterialToList(m, list);
            }
        }
        //bartworks
        LinkedHashSet<Werkstoff> bwMaterials = Werkstoff.werkstoffHashSet;
        for (Werkstoff m : bwMaterials) {
            if (m != null) {
                dumpMaterialToList(m.getBridgeMaterial(), list);
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

    private static void dumpMaterialToList(Materials materials, LinkedList<String[]> list) {
        list.add(new String[]{
            String.valueOf(materials.mMetaItemSubID),
            materials.mDefaultLocalName,
            materials.mLocalizedName,
            String.valueOf(materials.mDurability),
            String.valueOf(materials.mToolSpeed),
            String.valueOf(materials.mToolQuality)
        });
    }
}
