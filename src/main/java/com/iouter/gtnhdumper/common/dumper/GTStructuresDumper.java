package com.iouter.gtnhdumper.common.dumper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;

import com.google.gson.JsonObject;
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;
import com.gtnewhorizon.structurelib.alignment.constructable.IConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureElement;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.iouter.gtnhdumper.GTNHDumper;
import com.iouter.gtnhdumper.common.utils.StructureDumpHelper;
import com.iouter.gtnhdumper.common.utils.StructureHacks;
import com.iouter.gtnhdumper.common.utils.Utils;

import blockrenderer6343.client.utils.ConstructableData;
import codechicken.nei.NEIClientUtils;
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
        NEIClientUtils.printChatMessage(dumpMessage(null));
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
                    GTNHDumper.GSON.toJson(new GTStructure(te, structureDefinition, constructable), writer);
                    GTNHDumper.info("已写入：" + file.getAbsolutePath());
                } catch (IOException e) {
                    file.deleteOnExit();
                    GTNHDumper.LOG.error(e);
                } catch (Exception e) {
                    GTNHDumper.info("导出" + simpleName + "时发生错误：" + e.getLocalizedMessage());
                    file.deleteOnExit();
                    e.printStackTrace();
                }
            });
    }

    @Override
    public int modeCount() {
        return 1;
    }

    @Override
    public ChatComponentTranslation dumpMessage(File file) {
        return new ChatComponentTranslation("nei.options.tools.dump.gtnhdumper.gtstructure.dumped");
    }

    private static class GTStructure {

        private final int meta;
        private Map<String, String[][]> shapes;
        private final Map<Character, Object> elements = new LinkedHashMap<>();

        public GTStructure(IMetaTileEntity te, StructureDefinition<?> structureDefinition,
            IConstructable constructable) {
            meta = te.getBaseMetaTileEntity()
                .getMetaTileID();
            shapes = StructureDumpHelper.decodeShapes(structureDefinition);
            for (Map.Entry<Character, ? extends IStructureElement<?>> entry : structureDefinition.getElements()
                .entrySet()) {
                Character ch = entry.getKey();
                if (ch >= '퀀' && ch <= '\udfff') {
                    continue;
                }
                if (ch.equals('-')) {
                    continue;
                }
                JsonObject json = new JsonObject();
                @SuppressWarnings("unchecked")
                IStructureElement<IMetaTileEntity> element = (IStructureElement<IMetaTileEntity>) entry.getValue();
                var blocks = StructureHacks
                    .getStacksForElement(te, element, ConstructableData.getTierData(constructable));
                String channel = StructureHacks.getChannel(
                    element.getClass()
                        .getName(),
                    element);
                if (!(channel.isEmpty())) {
                    json.addProperty("channel", channel);
                }
                ItemStack stackTarget = new ItemStack(Item.getItemById(1));
                List<ItemStack> itemStacks;
                if (blocks != null) {
                    itemStacks = new ArrayList<>();
                    blocks.forEach(s -> {
                        if (s != null) {
                            itemStacks.add(s);
                        }
                    });
                    if (!itemStacks.isEmpty()) {
                        stackTarget = itemStacks.get(0);
                        json.add("blocks", GTNHDumper.GSON.toJsonTree(itemStacks));
                    }
                } else {
                    itemStacks = null;
                }
                int dot = StructureDumpHelper.getDotForElement(te, element);
                if (dot != -1) {
                    json.addProperty("dot", dot);
                }
                @SuppressWarnings("unchecked")
                IStructureElement<Object> elementObj = (IStructureElement<Object>) entry.getValue();
                var obj = StructureDumpHelper.scanCandidates(te, elementObj, stackTarget, new BlockPos());
                if (obj instanceof Set) {
                    @SuppressWarnings("unchecked")
                    Set<ItemStack> stackSet = (Set<ItemStack>) obj;
                    json.add("hatch", GTNHDumper.GSON.toJsonTree(stackSet));
                } else if (obj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<List<ItemStack>> hatchesList = (List<List<ItemStack>>) obj;
                    if (itemStacks != null) {
                        Set<List<ItemStack>> newHatchList = new LinkedHashSet<>();
                        for (var hatches : hatchesList) {
                            boolean isInvaild = false;
                            for (var hatch : hatches) {
                                if ((itemStacks.stream()
                                    .anyMatch(stack -> Utils.isItemEqual(stack, hatch)))) {
                                    isInvaild = true;
                                    break;
                                }
                            }
                            if (!isInvaild) {
                                newHatchList.add(hatches);
                            }
                        }
                        if (!(newHatchList.isEmpty())) {
                            json.add("hatch", GTNHDumper.GSON.toJsonTree(newHatchList));
                        }
                    } else {
                        if (!(hatchesList.isEmpty())) {
                            json.add("hatch", GTNHDumper.GSON.toJsonTree(hatchesList));
                        }
                    }
                }
                if (!json.isEmpty()) elements.put(ch, json);
            }
        }
    }
}
