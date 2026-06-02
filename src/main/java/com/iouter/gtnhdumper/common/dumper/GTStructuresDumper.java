package com.iouter.gtnhdumper.common.dumper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import com.gtnewhorizon.structurelib.alignment.constructable.IConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureElement;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.iouter.gtnhdumper.GTNHDumper;
import com.iouter.gtnhdumper.common.utils.StructureDecoder;

import codechicken.nei.config.DataDumper;
import gregtech.api.GregTechAPI;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;

public class GTStructuresDumper extends DataDumper {

    public GTStructuresDumper() {
        super("tools.dump.gtnhdumper.gtstructure");
    }

    @Override
    public String[] header() {
        return null;
    }

    @Override
    public Iterable<String[]> dump(int mode) {
        return null;
    }

    @Override
    public void dumpFile() {
        dumpStructures();
    }

    private void dumpStructures() {
        Arrays.stream(GregTechAPI.METATILEENTITIES)
            .filter(te -> te instanceof IConstructable)
            .forEach(te -> {
                IConstructable constructable = (IConstructable) te;
                if (!(constructable.getStructureDefinition() instanceof StructureDefinition<?>structureDefinition)) {
                    return;
                }
                final String simpleName = te.getClass()
                    .getSimpleName();
                final String name = "dumps/structures/" + simpleName + ".json";
                File file = new File(name);
                File parentDir = file.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                try (FileWriter writer = new FileWriter(file)) {
                    GTNHDumper.GSON.toJson(new GTStructure(te, structureDefinition), writer);
                    GTNHDumper.info("已写入：" + file.getAbsolutePath());
                } catch (IOException e) {
                    file.deleteOnExit();
                    GTNHDumper.LOG.error(e);
                } catch (Exception e) {
                    GTNHDumper.info("导出" + simpleName + "时发生错误：" + e.getLocalizedMessage());
                    file.deleteOnExit();
                    GTNHDumper.LOG.error(e);
                }
            });
    }

    private static class GTStructure {

        private final int meta;
        private Map<String, String[][]> shapes;
        private final Map<Character, Object> elements = new LinkedHashMap<>();

        public GTStructure(IMetaTileEntity te, StructureDefinition<?> structureDefinition) {
            meta = te.getBaseMetaTileEntity()
                .getMetaTileID();
            shapes = StructureDecoder.decodeShapes(structureDefinition);
            for (Map.Entry<Character, ? extends IStructureElement<?>> entry : structureDefinition.getElements()
                .entrySet()) {
                Character ch = entry.getKey();
                elements.put(ch, entry.getValue());
            }
        }
    }
}
