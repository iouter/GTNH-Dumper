package com.iouter.gtnhdumper.common;

import bartworks.system.material.Werkstoff;
import com.iouter.gtnhdumper.Utils;
import com.iouter.gtnhdumper.common.base.WikiDumper;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.MetaPipeEntity;
import gregtech.api.metatileentity.implementations.MTECable;
import gregtech.api.metatileentity.implementations.MTEFluidPipe;
import gregtech.api.metatileentity.implementations.MTEItemPipe;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTUtility;
import gtPlusPlus.core.material.Material;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.GTPPMTECable;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.GTPPMTEFluidPipe;
import ic2.core.item.resources.ItemCell;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.text.WordUtils;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.tools.ArrowMaterial;
import tconstruct.library.tools.BowMaterial;
import tconstruct.library.tools.ToolMaterial;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.iouter.gtnhdumper.common.TICMaterialDumper.getReinforcedString;
import static com.iouter.gtnhdumper.common.TICMaterialDumper.toRomaNumber;
import static tconstruct.library.TConstructRegistry.toolMaterials;

public class GTMaterialDumper extends WikiDumper {

    private static final String TRUE = "true";
    private static final String FALSE = "false";

    private static final String NAME = "Name";
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

    private static final String ORE_PREFIXES = "OrePrefixes";

    private static final String[] HEADER = {
        NAME,
        DEFAULT_NAME,
        LOCALIZED_NAME,
        CHEMICAL_FORMULA,
        FLAVOR_TEXT,
        DURABILITY,
        TOOL_SPEED,
        TOOL_QUALITY,
//        DUST_ITEMS,
//        METAL_ITEMS,
//        GEM_ITEMS,
//        ORE_ITEMS,
//        CELL,
//        PLASMA,
//        TOOL_HEAD_ITEMS,
//        GEAR_ITEMS,
//        EMPTY,
//        GAS,
//        FLUID,
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
        ORE_PREFIXES,
    };

    public GTMaterialDumper() {
        super("tools.dump.gtnhdumper.gtmaterial");
    }

    private static void getOrePrefixesMap(Function<OrePrefixes, ItemStack> stackSupplier, Map<String, Object> materialMap) {
        Map<String, Object> orePrefixesMap = null;
        if (materialMap.containsKey(ORE_PREFIXES)) {
            Object obj = materialMap.get(ORE_PREFIXES);
            if (obj instanceof Map) {
                orePrefixesMap = (Map<String, Object>) obj;
            }
        } else {
            orePrefixesMap = new LinkedHashMap<>();
        }
        if (orePrefixesMap == null) {
            return;
        }
        for (OrePrefixes prefix : OrePrefixes.VALUES) {
            ItemStack prefixStack = stackSupplier.apply(prefix);
            if (prefixStack == null) {
                continue;
            }
            String name = prefix.toString();
            putStackInOrePrefixesMap(orePrefixesMap, prefixStack, name);
            if (name.contains("cell")) {
                FluidStack fluidStack = GTUtility.getFluidForFilledItem(prefixStack, true);
                if (fluidStack == null) {
                    continue;
                }
                String fluidStackName = "fluid." + fluidStack.getFluid().getName();
                String nameFluid = name.replace("cell", "fluid");
                Object obj = orePrefixesMap.get(nameFluid);
                if (obj == null) {
                    orePrefixesMap.put(nameFluid, fluidStackName);
                } else if (obj instanceof String) {
                    String current = (String) obj;
                    if (!current.equals(fluidStackName))
                        orePrefixesMap.put(nameFluid, new String[] {current, fluidStackName});
                } else if (obj instanceof String[]) {
                    String[] currents = (String[]) obj;
                    if (Arrays.stream(currents).noneMatch(str -> str.equals(fluidStackName))) {
                        orePrefixesMap.put(nameFluid, Stream.concat(Arrays.stream(currents), Stream.of(fluidStackName)).toArray(String[]::new));
                    };
                }
            }

        }
        if (orePrefixesMap.isEmpty()) {
            materialMap.put(ORE_PREFIXES, null);
            return;
        }
        materialMap.put(ORE_PREFIXES, orePrefixesMap);
    }

    private static void putStackInOrePrefixesMap(Map<String, Object> orePrefixesMap, ItemStack prefixStack, String prefixName) {
        Object prefixObj = orePrefixesMap.get(prefixName);
        if (prefixObj instanceof ItemStack) {
            ItemStack mapStack = (ItemStack) prefixObj;
            if (!Utils.isItemStackEqual(mapStack, prefixStack)){
                orePrefixesMap.put(prefixName, new ItemStack[] {mapStack, prefixStack});
            }
        } else if (prefixObj instanceof ItemStack[]) {
            ItemStack[] prefixStacks = (ItemStack[]) prefixObj;
            if (!Utils.isStacksContain(prefixStack, prefixStacks)) {
                orePrefixesMap.put(prefixName, Stream.concat(Arrays.stream(prefixStacks), Stream.of(prefixStack)).toArray(ItemStack[]::new));
            }
        } else {
            orePrefixesMap.put(prefixName, prefixStack);
        }
    }

    private static Map<String, Object> getMaterialMap(String defaultLocalName, Map<String, Map<String, Object>> totalMap) {
        return totalMap.get(defaultLocalName) != null ? totalMap.get(defaultLocalName) : new HashMap<>();
    }

    private static void dumpGTMaterial(Materials m, Map<String, Map<String, Object>> totalMap) {
        String name = m.getName();
        Map<String, Object> materialMap = getMaterialMap(name, totalMap);
        materialMap.put(NAME, name);
        materialMap.put(DEFAULT_NAME, m.mDefaultLocalName);
        materialMap.put(LOCALIZED_NAME, m.mLocalizedName);
        materialMap.put(CHEMICAL_FORMULA, m.mChemicalFormula);
        materialMap.put(FLAVOR_TEXT, m.flavorText);
        materialMap.put(DURABILITY, m.mDurability);
        materialMap.put(TOOL_SPEED, m.mToolSpeed);
        materialMap.put(TOOL_QUALITY, m.mToolQuality);
//        if (m.hasEmpty())
//            materialMap.put(EMPTY, TRUE);
        getOrePrefixesMap(orePrefixes -> GTOreDictUnificator.get(orePrefixes, m, 1), materialMap);
//        if (m.hasCorrespondingGas())
//            materialMap.put(GAS, TRUE);
//        if (m.hasCorrespondingFluid())
//            materialMap.put(FLUID, TRUE);
        putModName(materialMap, "GregTech");
        totalMap.put(name, materialMap);
    }

    private static void dumpBartMaterial(Werkstoff w, Map<String, Map<String, Object>> totalMap) {
        Materials m = w.getBridgeMaterial();
        String name = m.getName();
        Map<String, Object> materialMap = getMaterialMap(name, totalMap);
        materialMap.put(NAME, name);
        materialMap.put(DEFAULT_NAME, m.mDefaultLocalName);
        materialMap.put(LOCALIZED_NAME, m.mLocalizedName);
        materialMap.put(CHEMICAL_FORMULA, m.mChemicalFormula);
        materialMap.put(FLAVOR_TEXT, m.flavorText);
        materialMap.put(DURABILITY, m.mDurability);
        materialMap.put(TOOL_SPEED, m.mToolSpeed);
        materialMap.put(TOOL_QUALITY, m.mToolQuality);
        getOrePrefixesMap(orePrefixes -> {
            if (w.hasItemType(orePrefixes)) {
                return w.get(orePrefixes);
            }
            return null;
        }, materialMap);
        putModName(materialMap, w.getOwner());
        totalMap.put(name, materialMap);
    }

    private static void dumpGTPPMaterial(Material m, Map<String, Map<String, Object>> totalMap) {
        String name = m.getLocalizedName();
        Map<String, Object> materialMap = getMaterialMap(name, totalMap);
        materialMap.put(NAME, name);
        materialMap.put(DEFAULT_NAME, name);
        materialMap.put(LOCALIZED_NAME, m.getTranslatedName());
        materialMap.put(CHEMICAL_FORMULA, m.vChemicalFormula);
//            materialMap.put("Durability", String.valueOf(m.vDurability));
//            materialMap.put("ToolSpeed", String.valueOf(m.vHarvestLevel * 2 + m.vTier));
//            materialMap.put("ToolQuality", String.valueOf(m.vToolQuality));
        getOrePrefixesMap(orePrefixes -> m.getComponentByPrefix(orePrefixes, 1), materialMap);
//        if (m.getFluid() != null)
//            switch (m.getState()) {
//                case GAS:
//                case PURE_GAS:
//                    materialMap.put(GAS, TRUE);
//                    break;
//                case PLASMA:
//                    materialMap.put(PLASMA, TRUE);
//                    break;
//                default:
//                    materialMap.put(FLUID, TRUE);
//                    break;
//            }
        putModName(materialMap, "GT++");
        totalMap.put(name, materialMap);
    }

    private static void putModName(Map<String, Object> materialMap, String modName) {
        if (modName == null) modName = "BartWorks";
        materialMap.merge(MOD, modName, (a, b) -> a + ARRAY_SEPARATOR + b);
    }

    private static void dumpPipeEntity(MetaPipeEntity pipeEntity, Map<String, Map<String, Object>> totalMap) {
        String name = null;
        Map<String, Object> materialMap = null;
        if (pipeEntity instanceof MTEFluidPipe) {
            MTEFluidPipe fluidPipe = (MTEFluidPipe) pipeEntity;
            // Fluid
            Materials m = fluidPipe.mMaterial;
            if (m != null)
                name = fluidPipe.mMaterial.getName();
            else if (fluidPipe instanceof GTPPMTEFluidPipe) {
                GTPPMTEFluidPipe gtppFluidPipe = (GTPPMTEFluidPipe) fluidPipe;
                Material tempM = Material.mMaterialCache.get(gtppFluidPipe.pipeStats.defaultLocalName.toLowerCase());
                if (tempM != null)
                    name = tempM.getLocalizedName();
            }
            materialMap = getMaterialMap(name, totalMap);
            materialMap.put(PIPE, FLUID);
            String[] temp= fluidPipe.getMetaName().split("_");
            String fluidPipeType = WordUtils.capitalizeFully(temp[temp.length - 1]);
            if (!fluidPipeType.equals(QUADRUPLE) && !fluidPipeType.equals(NONUPLE) && !fluidPipeType.equals(TINY) && !fluidPipeType.equals(SMALL) && !fluidPipeType.equals(LARGE) && !fluidPipeType.equals(HUGE))
                fluidPipeType = NORMAL;
            fluidPipeType = FLUID + PIPE + fluidPipeType;
            int capacity = fluidPipe.getCapacity();
            materialMap.put(HEAT_RESISTANCE, fluidPipe.mHeatResistance);
            if (fluidPipe.mGasProof)
                materialMap.put(GAS_PROOF, TRUE);
            materialMap.put(fluidPipeType, capacity);
        } else if (pipeEntity instanceof MTEItemPipe) {
            MTEItemPipe itemPipe = (MTEItemPipe) pipeEntity;
            // Item
            name = itemPipe.mMaterial.getName();
            materialMap = getMaterialMap(name, totalMap);
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
            materialMap.put(itemPipeType, capacity);
            materialMap.put(itemPipeType + STEP_SIZE, itemPipe.mStepSize);

        } else if (pipeEntity instanceof MTECable) {
            MTECable cable = (MTECable) pipeEntity;
            //Cable
            Materials m = cable.mMaterial;
            if (m != null)
                name = cable.mMaterial.getName();
            else if (cable instanceof GTPPMTECable) {
                GTPPMTECable gtppCable = (GTPPMTECable) cable;
                String[] temp = gtppCable.getMetaName().split("\\.");
                String tempS = Arrays.stream(temp).skip(1).limit(temp.length - 2).collect(Collectors.joining("."));
                Material tempM = Material.mMaterialCache.get(tempS);
                if (tempM != null)
                    name = tempM.getLocalizedName();
            }
            materialMap = getMaterialMap(name, totalMap);
            materialMap.put(CABLE_VOLTAGE, cable.mVoltage);
            String[] temp = cable.getMetaName().split("\\.");
            String cableType = WordUtils.capitalizeFully(temp[0]);
            materialMap.put(cableType + LOSS, cable.mCableLossPerMeter);
            String cableSize = temp[temp.length - 1];
            materialMap.put(cableType + cableSize, cable.mAmperage);
        }
        if (name != null)
            totalMap.put(name, materialMap);
    }

    private static int getMaxPipeCapacity(int capacity) {
        return Math.max(1, capacity);
    }

    @Override
    public int getKeyIndex() {
        return 0;
    }

    @Override
    public String getKeyStr() {
        return "gtMaterials";
    }

    @Override
    public String[] header() {
        return HEADER;
    }

    @Override
    public Iterable<Object[]> dumpObject(int mode) {
        Map<String, Map<String, Object>> totalMap = new HashMap<>();
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
//        for (int index : toolMaterials.keySet()) {
//            ToolMaterial m = toolMaterials.get(index);
//            ArrowMaterial arrowMaterial = TConstructRegistry.getArrowMaterial(index);
//            BowMaterial bowMaterial = TConstructRegistry.getBowMaterial(index);
//            String mass = "", breakChance = "", drawspeed = "", flightSpeedMax = "";
//            if (arrowMaterial != null) {
//                mass = String.valueOf(arrowMaterial.mass);
//                breakChance = String.valueOf(arrowMaterial.breakChance);
//            }
//            if (bowMaterial != null) {
//                drawspeed = String.valueOf(bowMaterial.drawspeed);
//                flightSpeedMax = String.valueOf(bowMaterial.flightSpeedMax);
//            }
//            String ability = m.ability();
//            if (m.stonebound > 0 && ability != "") {
//                ability += " " + toRomaNumber((int) Math.abs(m.stonebound));
//            }
//            if (m.reinforced() > 0) {
//                if (ability != "") ability += " / ";
//                ability += getReinforcedString(m.reinforced());
//            }
//            String defaultLocalName = m.name();
//            Map<String, String> materialMap = getMaterialMap(defaultLocalName, totalMap);
//            materialMap.put(TINKER_BASE_DURABILITY, String.valueOf(m.durability()));
//            materialMap.put(TINKER_HANDLE_MODIFIER, String.valueOf(m.handleDurability()));
//            materialMap.put(TINKER_FULL_DURABILITY, String.valueOf(Math.round(m.durability() * m.handleDurability())));
//            materialMap.put(TINKER_MINING_SPEED, String.valueOf(m.toolSpeed() / 100F));
//            materialMap.put(TINKER_MINING_LEVEL, String.valueOf(m.harvestLevel()));
//            materialMap.put(TINKER_ATTACK_DAMAGE, String.valueOf(m.attack()));
//            materialMap.put(TINKER_ABILITY, ability);
//            materialMap.put(TINKER_DRAW_SPEED, drawspeed);
//            materialMap.put(TINKER_ARROW_SPEED, flightSpeedMax);
//            materialMap.put(TINKER_WEIGHT, mass);
//            materialMap.put(TINKER_BREAK_CHANCE, breakChance);
//        }
        return totalMap.values()
            .stream()
            .map(innerMap -> Arrays.stream(header())
                .map(key -> innerMap.getOrDefault(key, null))
                .toArray(Object[]::new))
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public ChatComponentTranslation dumpMessage(File file) {
        return new ChatComponentTranslation(
            "nei.options.tools.dump.gtnhdumper.gtmaterial.dumped", "dumps/" + file.getName());
    }
}
