package com.iouter.gtnhdumper.common.dumper;

import codechicken.nei.config.DataDumper;
import com.gtnewhorizon.structurelib.alignment.constructable.IConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureElement;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.iouter.gtnhdumper.GTNHDumper;
import gregtech.api.GregTechAPI;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GTStructuresDumper extends DataDumper {
    private static final char SEP_COL_INNER = '퀀';   // 퀀 ：同一列内行之间的分隔符
    private static final char SEP_CELL_MERGE = '퀁';  // 퀁 ：单元格内连接符，将前后文本用 "~" 连接
    private static final char SEP_COL_END   = '퀂';   // 퀂 ：列结束符

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
        Arrays.stream(GregTechAPI.METATILEENTITIES).filter(te -> te instanceof IConstructable).forEach(
            te -> {
                IConstructable constructable = (IConstructable) te;
                if (!(constructable.getStructureDefinition() instanceof StructureDefinition<?>)) {
                    return;
                }
                StructureDefinition<?> structureDefinition = (StructureDefinition<?>) constructable.getStructureDefinition();
                final String simpleName = te.getClass().getSimpleName();
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
            }
        );
    }

    private static class GTStructure{
        private final int meta;
        private final Map<String, String> shapes = new HashMap<>();
        private final Map<Character, Object> elements = new HashMap<>();

        public GTStructure(IMetaTileEntity te, StructureDefinition<?> structureDefinition) {
            meta = te.getBaseMetaTileEntity().getMetaTileID();
            for (Map.Entry<String, String> entry : structureDefinition.getShapes().entrySet()) {
                shapes.put(entry.getKey(), entry.getValue());
            }
            for (Map.Entry<Character, ? extends IStructureElement<?>> entry : structureDefinition.getElements().entrySet()) {
                Character c = entry.getKey();
                if (c.equals(SEP_CELL_MERGE) || c.equals(SEP_COL_END) || c.equals(SEP_COL_INNER)) {
                    continue;
                }
                elements.put(c, entry.getValue());
            }
        }
    }
}
