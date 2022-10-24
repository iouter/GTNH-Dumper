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
            "ID",
            "Name",
            "Localized Name",
            "Base Durability",
            "Handle Modifier",
            "Full Durability",
            "Mining Speed",
            "Mining Level",
            "Attack Damage",
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
            list.add(new String[] {
                String.valueOf(index),
                m.name(),
                m.prefixName(),
                String.valueOf(m.durability()),
                String.valueOf(m.handleDurability()),
                String.valueOf(Math.round(m.durability() * m.handleDurability())),
                String.valueOf(m.toolSpeed() / 100F),
                String.valueOf(m.harvestLevel()),
                String.valueOf(m.attack()),
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
        if (reinforced > 9) return StatCollector.translateToLocal("tool.unbreakable");
        String ret = StatCollector.translateToLocal("tool.reinforced") + " ";
        ret += toRomaNumber(reinforced);
        return ret;
    }

    public static String toRomaNumber(int num) {
        switch (num) {
            case 1:
                return "I";
            case 2:
                return "II";
            case 3:
                return "III";
            case 4:
                return "IV";
            case 5:
                return "V";
            case 6:
                return "VI";
            case 7:
                return "VII";
            case 8:
                return "VIII";
            case 9:
                return "IX";
            case 10:
                return "X";
            default:
                return String.valueOf(num);
        }
    }
}
