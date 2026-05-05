package com.iouter.gtnhdumper.common.dumper;

import codechicken.nei.config.DataDumper;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;

import java.util.HashMap;
import java.util.Map;

public class GTStructuresDumper extends DataDumper {
    public GTStructuresDumper(String name) {
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

    private <T> Map<String, Object> get(IStructureDefinition<T> structureDefinition) {
        Map<String, Object> map = new HashMap<>();

        return map;
    }
}
