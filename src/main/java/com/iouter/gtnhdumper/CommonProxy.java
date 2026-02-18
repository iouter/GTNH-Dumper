package com.iouter.gtnhdumper;

import com.iouter.gtnhdumper.common.utils.KeySimulator;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

public class CommonProxy {

    public static boolean isGTLoaded = Loader.isModLoaded("gregtech");
    public static boolean isTiCLoaded = Loader.isModLoaded("TConstruct");
    public static boolean isTCLoaded = Loader.isModLoaded("Thaumcraft");
    public static boolean isAutomagyLoaded = Loader.isModLoaded("Automagy");
    public static boolean isTCNEIAdditionsLoaded = Loader.isModLoaded("tcneiadditions");
    public static boolean isAvaritiaLoaded = Loader.isModLoaded("Avaritia");


    // preInit "Run before anything else. Read your config, create blocks, items,
    // etc, and register them with the GameRegistry."
    public void preInit(FMLPreInitializationEvent event) {
        GTNHDumper.info(Tags.MODNAME + ": time for dump data");
    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes."
    public void init(FMLInitializationEvent event) {}

    // postInit "Handle interaction with other mods, complete your setup based on this."
    public void postInit(FMLPostInitializationEvent event) {
        KeySimulator.test();
    }

    public void serverAboutToStart(FMLServerAboutToStartEvent event) {}

    // register server commands in this event handler
    public void serverStarting(FMLServerStartingEvent event) {}

    public void serverStarted(FMLServerStartedEvent event) {}

    public void serverStopping(FMLServerStoppingEvent event) {}

    public void serverStopped(FMLServerStoppedEvent event) {}
}
