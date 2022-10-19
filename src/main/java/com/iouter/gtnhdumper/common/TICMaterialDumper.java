package com.iouter.gtnhdumper.common;

import static tconstruct.library.TConstructRegistry.toolMaterials;

import codechicken.nei.config.DataDumper;
import java.io.File;
import java.util.LinkedList;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.tools.ArrowMaterial;
import tconstruct.library.tools.BowMaterial;
import tconstruct.library.tools.ToolMaterial;

public class TICMaterialDumper extends DataDumper {
    public TICMaterialDumper() {
        super("tools.dump.gtnhdumper.ticmaterial");
    }

    @Override
    public String[] header() {
        return new String[] {
            "Name",
            "Localized Name",
            "Base Durability",
            "Handle Modifier",
            "Full Durability",
            "Mining Speed",
            "Mining Level",
            "Attack Damage",
            "Reinforced",
            "Ability",
            "Draw Speed",
            "Arrow Speed",
            "Weight",
            "Break Chance"
        };
    }

    @Override
    public Iterable<String[]> dump(int mode) {
        LinkedList<String[]> list = new LinkedList<>();
        for (int index : toolMaterials.keySet()) {
            ToolMaterial m = toolMaterials.get(index);
            ArrowMaterial arrowMaterial = TConstructRegistry.getArrowMaterial(index);
            BowMaterial bowMaterial = TConstructRegistry.getBowMaterial(index);
            String mass = "", breakChance = "", drawspeed = "", flightSpeedMax = "", reinforced = "";
            String ability = m.ability();
            if (m.stonebound > 0) {
                ability += " (" + Math.abs(m.stonebound) + ")";
            }
            if (arrowMaterial != null) {
                mass = String.valueOf(arrowMaterial.mass);
                breakChance = String.valueOf(arrowMaterial.breakChance);
            }
            if (bowMaterial != null) {
                drawspeed = String.valueOf(bowMaterial.drawspeed);
                flightSpeedMax = String.valueOf(bowMaterial.flightSpeedMax);
            }
            if (m.reinforced() > 0) {
                reinforced = getReinforcedString(m.reinforced());
            }
            list.add(new String[] {
                m.name(),
                m.prefixName(),
                String.valueOf(m.durability()),
                String.valueOf(m.handleDurability()),
                String.valueOf(Math.round(m.durability() * m.handleDurability())),
                String.valueOf(m.toolSpeed() / 100F),
                String.valueOf(m.harvestLevel()),
                String.valueOf(m.attack()),
                reinforced,
                ability,
                drawspeed,
                flightSpeedMax,
                mass,
                breakChance
            });
        }
        return list;
    }

    @Override
    public ChatComponentTranslation dumpMessage(File file) {
        return new ChatComponentTranslation(
                "nei.options.tools.dump.gtnhdumper.ticmaterial.dumped", "dumps/" + file.getName());
    }

    @Override
    public int modeCount() {
        return 1;
    }

    public static String getReinforcedString(int reinforced) {
        if (reinforced > 9) return StatCollector.translateToLocal("item.unbreakable");
        String ret = StatCollector.translateToLocal("tool.reinforced") + " ";
        switch (reinforced) {
            case 1:
                ret += "I";
                break;
            case 2:
                ret += "II";
                break;
            case 3:
                ret += "III";
                break;
            case 4:
                ret += "IV";
                break;
            case 5:
                ret += "V";
                break;
            case 6:
                ret += "VI";
                break;
            case 7:
                ret += "VII";
                break;
            case 8:
                ret += "VIII";
                break;
            case 9:
                ret += "IX";
                break;
            default:
                ret += reinforced;
                break;
        }
        return ret;
    }
}
