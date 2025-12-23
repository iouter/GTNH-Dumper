package com.iouter.gtnhdumper.common;

import bartworks.system.material.Werkstoff;
import codechicken.nei.config.DataDumper;
import gregtech.GTMod;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.Materials;
import gregtech.api.interfaces.metatileentity.IFluidLockable;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.MetaPipeEntity;
import gregtech.api.metatileentity.implementations.MTECable;
import gregtech.api.metatileentity.implementations.MTEFluidPipe;
import gregtech.api.metatileentity.implementations.MTEItemPipe;
import gregtech.api.util.GTLog;
import gregtech.common.blocks.ItemMachines;
import gtPlusPlus.core.material.Material;
import gtPlusPlus.core.material.state.MaterialState;
import gtPlusPlus.core.util.minecraft.ItemUtils;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.GTPPMTECable;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.GTPPMTEFluidPipe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import org.apache.commons.lang3.text.WordUtils;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.tools.ArrowMaterial;
import tconstruct.library.tools.BowMaterial;
import tconstruct.library.tools.ToolMaterial;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

import static com.iouter.gtnhdumper.common.TICMaterialDumper.getReinforcedString;
import static com.iouter.gtnhdumper.common.TICMaterialDumper.toRomaNumber;
import static tconstruct.library.TConstructRegistry.toolMaterials;

public class GTMaterialDumper extends DataDumper {

    private static final String TRUE = "true";
    private static final String FALSE = "false";

    private static final String DEFAULT_NAME = "DefaultName";
    private static final String LOCALIZED_NAME = "LocalizedName";

    private static final String CHEMICAL_FORMULA = "ChemicalFormula";
    private static final String FLAVOR_TEXT = "FlavorText";

    private static final String DURABILITY = "Durability";
    private static final String TOOL_SPEED = "ToolSpeed";
    private static final String TOOL_QUALITY = "ToolQuality";

    private static final String DUST_ITEMS = "DustItems";
    private static final String METAL_ITEMS = "MetalItems";
    private static final String GEM_ITEMS = "GemItems";
    private static final String ORE_ITEMS = "OreItems";
    private static final String CELL = "Cell";
    private static final String PLASMA = "Plasma";
    private static final String TOOL_HEAD_ITEMS = "ToolHeadItems";
    private static final String GEAR_ITEMS = "GearItems";
    private static final String EMPTY = "Empty";
    private static final String GAS = "Gas";
    private static final String FLUID = "Fluid";

    private static final String MOD = "Mod";

    private static final String PIPE = "Pipe";
    private static final String QUADRUPLE = "Quadruple";
    private static final String NONUPLE = "Nonuple";
    private static final String TINY = "Tiny";
    private static final String SMALL = "Small";
    private static final String LARGE = "Large";
    private static final String HUGE = "Huge";
    private static final String NORMAL = "Normal";
    private static final String HEAT_RESISTANCE = "HeatResistance";
    private static final String GAS_PROOF = "GasProof";

    private static final String ITEM = "Item";
    private static final String RESTRICTIVE = "Restrictive";
    private static final String STEP_SIZE = "StepSize";

    private static final String CABLE_VOLTAGE = "CableVoltage";
    private static final String LOSS = "Loss";

    private static final String TINKER_BASE_DURABILITY = "TinkerBaseDurability";
    private static final String TINKER_HANDLE_MODIFIER = "TinkerHandleModifier";
    private static final String TINKER_FULL_DURABILITY = "TinkerFullDurability";
    private static final String TINKER_MINING_SPEED = "TinkerMiningSpeed";
    private static final String TINKER_MINING_LEVEL = "TinkerMiningLevel";
    private static final String TINKER_ATTACK_DAMAGE = "TinkerAttackDamage";
    private static final String TINKER_ABILITY = "TinkerAbility";
    private static final String TINKER_DRAW_SPEED = "TinkerDrawSpeed";
    private static final String TINKER_ARROW_SPEED = "TinkerArrowSpeed";
    private static final String TINKER_WEIGHT = "TinkerWeight";
    private static final String TINKER_BREAK_CHANCE = "TinkerBreakChance";

    private static final String[] HEADER = {
        DEFAULT_NAME,
        LOCALIZED_NAME,
        CHEMICAL_FORMULA,
        FLAVOR_TEXT,
        DURABILITY,
        TOOL_SPEED,
        TOOL_QUALITY,
        DUST_ITEMS,
        METAL_ITEMS,
        GEM_ITEMS,
        ORE_ITEMS,
        CELL,
        PLASMA,
        TOOL_HEAD_ITEMS,
        GEAR_ITEMS,
        EMPTY,
        GAS,
        FLUID,
        MOD,
        PIPE,
        FLUID + PIPE + TINY,
        FLUID + PIPE + SMALL,
        FLUID + PIPE + NORMAL,
        FLUID + PIPE + LARGE,
        FLUID + PIPE + HUGE,
        FLUID + PIPE + QUADRUPLE,
        FLUID + PIPE + NONUPLE,
        HEAT_RESISTANCE,
        GAS_PROOF,
        ITEM + PIPE + TINY,
        ITEM + PIPE + TINY + STEP_SIZE,
        ITEM + PIPE + RESTRICTIVE + TINY,
        ITEM + PIPE + RESTRICTIVE + TINY + STEP_SIZE,
        ITEM + PIPE + SMALL,
        ITEM + PIPE + SMALL + STEP_SIZE,
        ITEM + PIPE + RESTRICTIVE + SMALL,
        ITEM + PIPE + RESTRICTIVE + SMALL + STEP_SIZE,
        ITEM + PIPE + NORMAL,
        ITEM + PIPE + NORMAL + STEP_SIZE,
        ITEM + PIPE + RESTRICTIVE + NORMAL,
        ITEM + PIPE + RESTRICTIVE + NORMAL + STEP_SIZE,
        ITEM + PIPE + LARGE,
        ITEM + PIPE + LARGE + STEP_SIZE,
        ITEM + PIPE + RESTRICTIVE + LARGE,
        ITEM + PIPE + RESTRICTIVE + LARGE + STEP_SIZE,
        ITEM + PIPE + HUGE,
        ITEM + PIPE + HUGE + STEP_SIZE,
        ITEM + PIPE + RESTRICTIVE + HUGE,
        ITEM + PIPE + RESTRICTIVE + HUGE + STEP_SIZE,
        CABLE_VOLTAGE,
        "Wire" + LOSS,
        "Cable" + LOSS,
        "Wire01",
        "Wire02",
        "Wire04",
        "Wire08",
        "Wire12",
        "Wire16",
        "Cable01",
        "Cable02",
        "Cable04",
        "Cable08",
        "Cable12",
        "Cable16",
//        TINKER_BASE_DURABILITY,
//        TINKER_HANDLE_MODIFIER,
//        TINKER_FULL_DURABILITY,
//        TINKER_MINING_SPEED,
//        TINKER_MINING_LEVEL,
//        TINKER_ATTACK_DAMAGE,
//        TINKER_ABILITY,
//        TINKER_DRAW_SPEED,
//        TINKER_ARROW_SPEED,
//        TINKER_WEIGHT,
//        TINKER_BREAK_CHANCE
    };

    public GTMaterialDumper() {
        super("tools.dump.gtnhdumper.gtmaterial");
    }

    private static Map<String, String> getMaterialMap(String defaultLocalName, Map<String, Map<String, String>> totalMap) {
        return totalMap.get(defaultLocalName) != null ? totalMap.get(defaultLocalName) : new HashMap<>();
    }

    private static void dumpGTMaterial(Materials m, Map<String, Map<String, String>> totalMap) {
        String defaultLocalName = m.mDefaultLocalName;
        Map<String, String> materialMap = getMaterialMap(defaultLocalName, totalMap);
        materialMap.put(DEFAULT_NAME, defaultLocalName);
        materialMap.put(LOCALIZED_NAME, m.mLocalizedName);
        materialMap.put(CHEMICAL_FORMULA, m.mChemicalFormula);
        materialMap.put(FLAVOR_TEXT, m.flavorText);
        materialMap.put(DURABILITY, String.valueOf(m.mDurability));
        materialMap.put(TOOL_SPEED, String.valueOf(m.mToolSpeed));
        materialMap.put(TOOL_QUALITY, String.valueOf(m.mToolQuality));
        if (m.hasDustItems())
            materialMap.put(DUST_ITEMS, TRUE);
        if (m.hasMetalItems())
            materialMap.put(METAL_ITEMS, TRUE);
        if (m.hasGemItems())
            materialMap.put(GEM_ITEMS, TRUE);
        if (m.hasOresItems())
            materialMap.put(ORE_ITEMS, TRUE);
        if (m.hasCell())
            materialMap.put(CELL, TRUE);
        if (m.hasPlasma())
            materialMap.put(PLASMA, TRUE);
        if (m.hasToolHeadItems())
            materialMap.put(TOOL_HEAD_ITEMS, TRUE);
        if (m.hasGemItems())
            materialMap.put(GEAR_ITEMS, TRUE);
        if (m.hasEmpty())
            materialMap.put(EMPTY, TRUE);
        if (m.hasCorrespondingGas())
            materialMap.put(GAS, TRUE);
        if (m.hasCorrespondingFluid())
            materialMap.put(FLUID, TRUE);
        putModName(materialMap, "GregTech");
        totalMap.put(defaultLocalName, materialMap);
    }

    private static void dumpBartMaterial(Werkstoff w, Map<String, Map<String, String>> totalMap) {
        Materials m = w.getBridgeMaterial();
        String defaultLocalName = m.mDefaultLocalName;
        Map<String, String> materialMap = getMaterialMap(defaultLocalName, totalMap);
        materialMap.put(DEFAULT_NAME, defaultLocalName);
        materialMap.put(LOCALIZED_NAME, m.mLocalizedName);
        materialMap.put(CHEMICAL_FORMULA, m.mChemicalFormula);
        materialMap.put(FLAVOR_TEXT, m.flavorText);
        materialMap.put(DURABILITY, String.valueOf(m.mDurability));
        materialMap.put(TOOL_SPEED, String.valueOf(m.mToolSpeed));
        materialMap.put(TOOL_QUALITY, String.valueOf(m.mToolQuality));
        if (m.hasDustItems())
            materialMap.put(DUST_ITEMS, TRUE);
        if (m.hasMetalItems())
            materialMap.put(METAL_ITEMS, TRUE);
        if (m.hasGemItems())
            materialMap.put(GEM_ITEMS, TRUE);
        if (m.hasOresItems())
            materialMap.put(ORE_ITEMS, TRUE);
        if (m.hasCell())
            materialMap.put(CELL, TRUE);
        if (m.hasPlasma())
            materialMap.put(PLASMA, TRUE);
        if (m.hasToolHeadItems())
            materialMap.put(TOOL_HEAD_ITEMS, TRUE);
        if (m.hasGemItems())
            materialMap.put(GEAR_ITEMS, TRUE);
        if (m.hasEmpty())
            materialMap.put(EMPTY, TRUE);
        if (m.hasCorrespondingGas())
            materialMap.put(GAS, TRUE);
        if (m.hasCorrespondingFluid())
            materialMap.put(FLUID, TRUE);
        putModName(materialMap, w.getOwner());
        totalMap.put(defaultLocalName, materialMap);
    }

    private static void dumpGTPPMaterial(Material m, Map<String, Map<String, String>> totalMap) {
        String defaultLocalName = m.getLocalizedName();
        Map<String, String> materialMap = getMaterialMap(defaultLocalName, totalMap);
        materialMap.put(DEFAULT_NAME, defaultLocalName);
        materialMap.put(LOCALIZED_NAME, m.getTranslatedName());
        materialMap.put(CHEMICAL_FORMULA, m.vChemicalFormula);
//            materialMap.put("Durability", String.valueOf(m.vDurability));
//            materialMap.put("ToolSpeed", String.valueOf(m.vHarvestLevel * 2 + m.vTier));
//            materialMap.put("ToolQuality", String.valueOf(m.vToolQuality));
        if (ItemUtils.checkForInvalidItems(new ItemStack[] {m.getDust(1)}))
            materialMap.put(DUST_ITEMS, TRUE);
        if (m.hasSolidForm())
            materialMap.put(METAL_ITEMS, TRUE);
        if (m.hasOre())
            materialMap.put(ORE_ITEMS, TRUE);
        if (ItemUtils.checkForInvalidItems(new ItemStack[] {m.getCell(1)}))
            materialMap.put(CELL, TRUE);
        if (m.getPlasma() != null)
            materialMap.put(PLASMA, TRUE);
        if (ItemUtils.checkForInvalidItems(new ItemStack[] {m.getGear(1)}))
            materialMap.put(GEAR_ITEMS, TRUE);
        if (m.getFluid() != null)
            switch (m.getState()) {
                case GAS:
                case PURE_GAS:
                    materialMap.put(GAS, TRUE);
                    break;
                case PLASMA:
                    materialMap.put(PLASMA, TRUE);
                    break;
                default:
                    materialMap.put(FLUID, TRUE);
                    break;
            }
        putModName(materialMap, "GT++");
        totalMap.put(defaultLocalName, materialMap);
    }

    private static void putModName(Map<String, String> materialMap, String modName) {
        if (modName == null) modName = "BartWorks";
        materialMap.merge(MOD, modName, (a, b) -> a + ", " + b);
    }

    private static void dumpPipeEntity(MetaPipeEntity pipeEntity, Map<String, Map<String, String>> totalMap) {
        String defaultLocalName = null;
        Map<String, String> materialMap = null;
        if (pipeEntity instanceof MTEFluidPipe) {
            MTEFluidPipe fluidPipe = (MTEFluidPipe) pipeEntity;
            // Fluid
            Materials m = fluidPipe.mMaterial;
            if (m != null)
                defaultLocalName = fluidPipe.mMaterial.mDefaultLocalName;
            else if (fluidPipe instanceof GTPPMTEFluidPipe) {
                GTPPMTEFluidPipe gtppFluidPipe = (GTPPMTEFluidPipe) fluidPipe;
                Material tempM = Material.mMaterialCache.get(gtppFluidPipe.pipeStats.defaultLocalName.toLowerCase());
                if (tempM != null)
                    defaultLocalName = tempM.getLocalizedName();
            }
            materialMap = getMaterialMap(defaultLocalName, totalMap);
            materialMap.put(PIPE, FLUID);
            String[] temp= fluidPipe.getMetaName().split("_");
            String fluidPipeType = WordUtils.capitalizeFully(temp[temp.length - 1]);
            if (!fluidPipeType.equals(QUADRUPLE) && !fluidPipeType.equals(NONUPLE) && !fluidPipeType.equals(TINY) && !fluidPipeType.equals(SMALL) && !fluidPipeType.equals(LARGE) && !fluidPipeType.equals(HUGE))
                fluidPipeType = NORMAL;
            fluidPipeType = FLUID + PIPE + fluidPipeType;
            int capacity = fluidPipe.getCapacity();
            materialMap.put(HEAT_RESISTANCE, String.valueOf(fluidPipe.mHeatResistance));
            if (fluidPipe.mGasProof)
                materialMap.put(GAS_PROOF, TRUE);
            materialMap.put(fluidPipeType, String.valueOf(capacity));
        } else if (pipeEntity instanceof MTEItemPipe) {
            MTEItemPipe itemPipe = (MTEItemPipe) pipeEntity;
            // Item
            defaultLocalName = itemPipe.mMaterial.mDefaultLocalName;
            materialMap = getMaterialMap(defaultLocalName, totalMap);
            materialMap.put(PIPE, ITEM);
            String[] temp = itemPipe.getMetaName().split("_");
            String itemPipeType = WordUtils.capitalizeFully(temp[temp.length - 1]);
            if (!itemPipeType.equals(TINY) && !itemPipeType.equals(SMALL) && !itemPipeType.equals(LARGE) && !itemPipeType.equals(HUGE))
                itemPipeType = NORMAL;
            if (itemPipe.mIsRestrictive)
                itemPipeType = RESTRICTIVE + itemPipeType;
            itemPipeType = ITEM + PIPE + itemPipeType;
            int tickTime = itemPipe.mTickTime;
            BigDecimal capacity = new BigDecimal(20 * getMaxPipeCapacity(itemPipe.getPipeCapacity())).divide(new BigDecimal(tickTime), 2, RoundingMode.HALF_UP);
            materialMap.put(itemPipeType, String.valueOf(capacity));
            materialMap.put(itemPipeType + STEP_SIZE, String.valueOf(itemPipe.mStepSize));

        } else if (pipeEntity instanceof MTECable) {
            MTECable cable = (MTECable) pipeEntity;
            //Cable
            Materials m = cable.mMaterial;
            if (m != null)
                defaultLocalName = cable.mMaterial.mDefaultLocalName;
            else if (cable instanceof GTPPMTECable) {
                GTPPMTECable gtppCable = (GTPPMTECable) cable;
                String[] temp = gtppCable.getMetaName().split("\\.");
                java.lang.String tempS = Arrays.stream(temp).skip(1).limit(temp.length - 2).collect(Collectors.joining("."));
                Material tempM = Material.mMaterialCache.get(tempS);
                if (tempM != null)
                    defaultLocalName = tempM.getLocalizedName();
            }
            materialMap = getMaterialMap(defaultLocalName, totalMap);
            materialMap.put(CABLE_VOLTAGE, String.valueOf(cable.mVoltage));
            String[] temp = cable.getMetaName().split("\\.");
            String cableType = WordUtils.capitalizeFully(temp[0]);
            materialMap.put(cableType + LOSS, String.valueOf(cable.mCableLossPerMeter));
            String cableSize = temp[temp.length - 1];
            materialMap.put(cableType + cableSize, String.valueOf(cable.mAmperage));
        }
        if (defaultLocalName != null)
            totalMap.put(defaultLocalName, materialMap);
    }

    private static int getMaxPipeCapacity(int capacity) {
        return Math.max(1, capacity);
    }

    @Override
    public String[] header() {
        return HEADER;
    }

    @Override
    public Iterable<String[]> dump(int mode) {
        Map<String, Map<String, String>> totalMap = new HashMap<>();
        //gt5
        Materials[] gtMaterials = Materials.values();
        for (Materials m : gtMaterials) {
            if (m != null) {
                dumpGTMaterial(m, totalMap);
            }
        }
        //bartworks
        for (Werkstoff m : Werkstoff.werkstoffHashSet) {
            if (m != null) {
                dumpBartMaterial(m, totalMap);
            }
        }
        // gtpp
        for (Material m : Material.mMaterialMap) {
            if (m != null)
                dumpGTPPMaterial(m, totalMap);
        }
        // Pipe and Wire
        for (IMetaTileEntity metaTileEntity : GregTechAPI.METATILEENTITIES) {
            if (metaTileEntity instanceof MetaPipeEntity) {
                MetaPipeEntity pipeEntity = (MetaPipeEntity) metaTileEntity;
                dumpPipeEntity(pipeEntity, totalMap);
            }
        }
        // Tinker
        for (int index : toolMaterials.keySet()) {
            ToolMaterial m = toolMaterials.get(index);
            ArrowMaterial arrowMaterial = TConstructRegistry.getArrowMaterial(index);
            BowMaterial bowMaterial = TConstructRegistry.getBowMaterial(index);
            String mass = "", breakChance = "", drawspeed = "", flightSpeedMax = "";
            if (arrowMaterial != null) {
                mass = String.valueOf(arrowMaterial.mass);
                breakChance = String.valueOf(arrowMaterial.breakChance);
            }
            if (bowMaterial != null) {
                drawspeed = String.valueOf(bowMaterial.drawspeed);
                flightSpeedMax = String.valueOf(bowMaterial.flightSpeedMax);
            }
            String ability = m.ability();
            if (m.stonebound > 0 && ability != "") {
                ability += " " + toRomaNumber((int) Math.abs(m.stonebound));
            }
            if (m.reinforced() > 0) {
                if (ability != "") ability += " / ";
                ability += getReinforcedString(m.reinforced());
            }
            String defaultLocalName = m.name();
            Map<String, String> materialMap = getMaterialMap(defaultLocalName, totalMap);
            materialMap.put(TINKER_BASE_DURABILITY, String.valueOf(m.durability()));
            materialMap.put(TINKER_HANDLE_MODIFIER, String.valueOf(m.handleDurability()));
            materialMap.put(TINKER_FULL_DURABILITY, String.valueOf(Math.round(m.durability() * m.handleDurability())));
            materialMap.put(TINKER_MINING_SPEED, String.valueOf(m.toolSpeed() / 100F));
            materialMap.put(TINKER_MINING_LEVEL, String.valueOf(m.harvestLevel()));
            materialMap.put(TINKER_ATTACK_DAMAGE, String.valueOf(m.attack()));
            materialMap.put(TINKER_ABILITY, ability);
            materialMap.put(TINKER_DRAW_SPEED, drawspeed);
            materialMap.put(TINKER_ARROW_SPEED, flightSpeedMax);
            materialMap.put(TINKER_WEIGHT, mass);
            materialMap.put(TINKER_BREAK_CHANCE, breakChance);
        }
        return totalMap.values()
            .stream()
            .map(innerMap -> Arrays.stream(HEADER)
                .map(key -> innerMap.getOrDefault(key, ""))
                .toArray(String[]::new))
            .collect(Collectors.toCollection(LinkedList::new));
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
