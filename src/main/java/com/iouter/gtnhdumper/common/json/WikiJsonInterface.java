package com.iouter.gtnhdumper.common.json;

import codechicken.nei.config.DataDumper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import journeymap.shadow.org.eclipse.jetty.client.WWWAuthenticationProtocolHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public interface WikiJsonInterface{

    int getKeyIndex();

    String getKeyStr();

    int getMode();

    String[] header();

    Iterable<String[]> dump(int mode);

    default String getFileExtensionWiki() {
        return ".json";
    }

    default int modeCountWiki() {
        return 2;
    }

    default String modeButtonTextWiki() {
        int mode = getMode();
        if (mode == 0) {
            return "List";
        } else {
            return "Map";
        }
    }

    default void dumpToWiki(File file) throws IOException {
        Object dumpObject;
        int mode = getMode();
        if (mode == 0) {
            dumpObject = WikiJsonUtil.getListJson(header(), dump(0), getKeyStr());
        } else {
            dumpObject = WikiJsonUtil.getMapJson(header(), dump(mode), getKeyIndex());
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(dumpObject, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
