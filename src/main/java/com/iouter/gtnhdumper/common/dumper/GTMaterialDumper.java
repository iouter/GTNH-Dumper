package com.iouter.gtnhdumper.common.dumper;

import bartworks.system.material.Werkstoff;
import com.iouter.gtnhdumper.Utils;
import com.iouter.gtnhdumper.common.base.WikiDumper;
import com.iouter.gtnhdumper.common.recipe.base.GTRecipeDumps;
import com.iouter.gtnhdumper.common.recipe.utils.Transformer;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.MetaPipeEntity;
import gregtech.api.metatileentity.implementations.MTECable;
import gregtech.api.metatileentity.implementations.MTEFluidPipe;
import gregtech.api.metatileentity.implementations.MTEItemPipe;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMapBackend;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTUtility;
import gtPlusPlus.core.material.Material;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.GTPPMTECable;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.GTPPMTEFluidPipe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private static final String ORE_PREFIXES = "OrePrefixes";

    public static final String ORE_TO_CRUSHED_MACERATOR = "OreToCrushedMacerator";
    public static final String RAW_ORE_TO_CRUSHED_MACERATOR = "RawOreToCrushedMacerator";
    public static final String CRUSHED_TO_CRUSHED_PURIFIED_ORE_WASHER = "CrushedToCrushedPurifiedOreWasher";
    public static final String CRUSHED_TO_CRUSHED_PURIFIED_CHEMICAL_BATH = "CrushedToCrushedPurifiedChemicalBath";
    public static final String CRUSHED_PURIFIED_TO_ANY_SIFTER = "CrushedPurifiedToAnySifter";
    public static final String CRUSHED_TO_DUST_IMPURE_MACERATOR = "CrushedToDustImpureMacerator";
    public static final String CRUSHED_PURIFIED_TO_CRUSHED_CENTRIFUGED_THERMAL_CENTRIFUGE = "CrushedPurifiedToCrushedCentrifugedThermalCentrifuge";
    public static final String CRUSHED_PURIFIED_TO_DUST_PURE_MACERATOR = "CrushedPurifiedToDustPureMacerator";
    public static final String DUST_IMPURE_TO_DUST_CENTRIFUGE = "DustImpureToDustCentrifuge";
    public static final String CRUSHED_CENTRIFUGED_TO_DUST_MACERATOR = "CrushedCentrifugedToDustMacerator";
    public static final String DUST_PURE_TO_DUST_CENTRIFUGE = "DustPureToDustCentrifuge";
    public static final String DUST_PURE_TO_DUST_ELECTRO_MAGNETIC_SEPARATOR = "DustPureToDustElectroMagneticSeparator";
    public static final String ORE_TO_FLUID_FLUID_EXTRACTOR = "OreToFluidFluidExtractor";
    private static final String ORE_PROCESSING = "OreProcessing";
    private static final String[] HEADER = {
        NAME,
        DEFAULT_NAME,
        LOCALIZED_NAME,
        CHEMICAL_FORMULA,
        FLAVOR_TEXT,
        DURABILITY,
        TOOL_SPEED,
        TOOL_QUALITY,
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
        ORE_PREFIXES,
        ORE_PROCESSING
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
        }
        if (orePrefixesMap == null) {
            orePrefixesMap = new LinkedHashMap<>();
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

    private static Map<String, Object> getOreProcessing(Map<String, Object> orePrefixesMap) {
        ItemStack ore = getItemStackFromMap(orePrefixesMap, OrePrefixes.ore);
        if (ore == null) {
            return null;
        }
        Map<String, Object> oreProcessingMap = new LinkedHashMap<>();
        addOreProcessing(oreProcessingMap, ORE_TO_CRUSHED_MACERATOR, orePrefixesMap, RecipeMaps.maceratorRecipes, OrePrefixes.crushed, OrePrefixes.ore);
        addOreProcessing(oreProcessingMap, RAW_ORE_TO_CRUSHED_MACERATOR, orePrefixesMap, RecipeMaps.maceratorRecipes, OrePrefixes.crushed, OrePrefixes.rawOre);
        addOreProcessing(oreProcessingMap, CRUSHED_TO_CRUSHED_PURIFIED_ORE_WASHER, orePrefixesMap, RecipeMaps.oreWasherRecipes, OrePrefixes.crushedPurified, OrePrefixes.crushed);
        addOreProcessing(oreProcessingMap, CRUSHED_TO_CRUSHED_PURIFIED_CHEMICAL_BATH, orePrefixesMap, RecipeMaps.chemicalBathRecipes, OrePrefixes.crushedPurified, OrePrefixes.crushed);
        addOreProcessing(true, oreProcessingMap, CRUSHED_PURIFIED_TO_ANY_SIFTER, orePrefixesMap, RecipeMaps.sifterRecipes, null, OrePrefixes.crushedPurified);
        addOreProcessing(oreProcessingMap, CRUSHED_TO_DUST_IMPURE_MACERATOR, orePrefixesMap, RecipeMaps.maceratorRecipes, OrePrefixes.dustImpure, OrePrefixes.crushed);
        addOreProcessing(oreProcessingMap, CRUSHED_PURIFIED_TO_CRUSHED_CENTRIFUGED_THERMAL_CENTRIFUGE, orePrefixesMap, RecipeMaps.thermalCentrifugeRecipes, OrePrefixes.crushedCentrifuged, OrePrefixes.crushedPurified);
        addOreProcessing(oreProcessingMap, CRUSHED_PURIFIED_TO_DUST_PURE_MACERATOR, orePrefixesMap, RecipeMaps.maceratorRecipes, OrePrefixes.dustPure, OrePrefixes.crushedPurified);
        addOreProcessing(oreProcessingMap, DUST_IMPURE_TO_DUST_CENTRIFUGE, orePrefixesMap, RecipeMaps.centrifugeRecipes, OrePrefixes.dust, OrePrefixes.dustImpure);
        addOreProcessing(oreProcessingMap, CRUSHED_CENTRIFUGED_TO_DUST_MACERATOR, orePrefixesMap, RecipeMaps.maceratorRecipes, OrePrefixes.dust, OrePrefixes.crushedCentrifuged);
        addOreProcessing(oreProcessingMap, DUST_PURE_TO_DUST_CENTRIFUGE, orePrefixesMap, RecipeMaps.centrifugeRecipes, OrePrefixes.dust, OrePrefixes.dustPure);
        addOreProcessing(oreProcessingMap, DUST_PURE_TO_DUST_ELECTRO_MAGNETIC_SEPARATOR, orePrefixesMap, RecipeMaps.electroMagneticSeparatorRecipes, OrePrefixes.dust, OrePrefixes.dustPure);
        GTRecipeDumps[] fluidExtractor = RecipeMaps
            .fluidExtractionRecipes
            .getAllRecipes()
            .stream()
            .filter(gtRecipe -> Arrays.stream(gtRecipe.mInputs).anyMatch(stack -> ore.getItem() == stack.getItem() && stack.getItemDamage() == ore.getItemDamage()))
            .filter(gtRecipe -> Arrays.stream(gtRecipe.mFluidOutputs).anyMatch(fluidStack -> {
                FluidStack cellFluid = GTUtility.getFluidForFilledItem(getItemStackFromMap(orePrefixesMap, OrePrefixes.cell), true);
                if (cellFluid == null) {
                    return false;
                }
                return cellFluid.getFluid() == fluidStack.getFluid();
            }))
            .map(Transformer::transformGTRecipe)
            .toArray(GTRecipeDumps[]::new);
        if (fluidExtractor.length != 0) {
            oreProcessingMap.put(ORE_TO_FLUID_FLUID_EXTRACTOR, fluidExtractor);
        }
        if (oreProcessingMap.isEmpty()) {
            return null;
        }
        return oreProcessingMap;
    }

    public static GTRecipeDumps[] findRecipes(boolean outputItemCanBeNull, Map<String, Object> orePrefixesMap, RecipeMap<RecipeMapBackend> recipes, OrePrefixes outputPrefix, OrePrefixes... inputPrefixes) {
        return findRecipes(outputItemCanBeNull, recipes,
            outputPrefix != null ? getItemStackFromMap(orePrefixesMap, outputPrefix) : null,
            Arrays.stream(inputPrefixes).map(prefix -> getItemStackFromMap(orePrefixesMap, prefix)).toArray(ItemStack[]::new));
    }

    public static GTRecipeDumps[] findRecipes(boolean outputItemCanBeNull, RecipeMap<RecipeMapBackend> recipes, ItemStack outputItem, ItemStack... inputItems) {
        if (inputItems == null || inputItems.length == 0 || Arrays.stream(inputItems).noneMatch(Objects::nonNull)) {
            return null;
        }
        if (!outputItemCanBeNull && outputItem == null) {
            return null;
        }
        return recipes.getAllRecipes().stream().filter(recipe -> {
            if (outputItem == null) {
                return true;
            }
            return Arrays.stream(recipe.mOutputs).anyMatch(stack -> {
                if (stack == null) {
                    return false;
                }
                return stack.getItem() == outputItem.getItem() && stack.getItemDamage() == outputItem.getItemDamage();
            });
        }).filter(recipe -> Arrays.stream(inputItems).filter(Objects::nonNull).allMatch(inputItem -> Arrays.stream(recipe.mInputs).anyMatch(stack -> {
            if (stack == null) {
                return false;
            }
            return inputItem.getItem() == stack.getItem() && inputItem.getItemDamage() == stack.getItemDamage();
        }))).map(Transformer::transformGTRecipe).toArray(GTRecipeDumps[]::new);
    }

    public static void addOreProcessing(Map<String, Object> oreProcessingMap, String processingKey, Map<String, Object> orePrefixesMap, RecipeMap<RecipeMapBackend> recipes, OrePrefixes outputPrefix, OrePrefixes... inputPrefixes) {
        addOreProcessing(false, oreProcessingMap, processingKey, orePrefixesMap, recipes, outputPrefix, inputPrefixes);
    }

    public static void addOreProcessing(boolean outputItemCanBeNull, Map<String, Object> oreProcessingMap, String processingKey, Map<String, Object> orePrefixesMap, RecipeMap<RecipeMapBackend> recipes, OrePrefixes outputPrefix, OrePrefixes... inputPrefixes) {
        GTRecipeDumps[] gtRecipe = findRecipes(outputItemCanBeNull, orePrefixesMap, recipes, outputPrefix, inputPrefixes);
        if (gtRecipe != null && gtRecipe.length > 0) {
            oreProcessingMap.put(processingKey, gtRecipe);
        }
    }

    private static ItemStack getItemStackFromMap(Map<String, Object> orePrefixesMap, OrePrefixes prefixes) {
        Object o = orePrefixesMap.get(prefixes.toString());
        if (o instanceof ItemStack) {
            return (ItemStack) o;
        }
        return null;
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
        getOrePrefixesMap(orePrefixes -> GTOreDictUnificator.get(orePrefixes, m, 1), materialMap);
        Object obj = materialMap.get(ORE_PREFIXES);
        if (obj instanceof Map) {
            materialMap.put(ORE_PROCESSING, getOreProcessing((Map<String, Object>) obj));
        }
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
        Object obj = materialMap.get(ORE_PREFIXES);
        if (obj instanceof Map) {
            materialMap.put(ORE_PROCESSING, getOreProcessing((Map<String, Object>) obj));
        }
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
        Object obj = materialMap.get(ORE_PREFIXES);
        if (obj instanceof Map) {
            materialMap.put(ORE_PROCESSING, getOreProcessing((Map<String, Object>) obj));
        }
        putModName(materialMap, "GT++");
        totalMap.put(name, materialMap);
    }

    private static void putModName(Map<String, Object> materialMap, String modName) {
        if (modName == null) modName = "BartWorks";
        materialMap.merge(MOD, modName, (a, b) -> {
            if (b.equals("GregTech")) {
                return b + ARRAY_SEPARATOR + a;
            }
            return a + ARRAY_SEPARATOR + b;
        });
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
        //gt5
        Materials[] gtMaterials = Materials.values();
        for (Materials m : gtMaterials) {
            if (m != null) {
                dumpGTMaterial(m, totalMap);
            }
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
