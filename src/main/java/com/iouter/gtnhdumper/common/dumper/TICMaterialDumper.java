package com.iouter.gtnhdumper.common.dumper;

import com.iouter.gtnhdumper.common.base.WikiDumper;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.tools.ArrowMaterial;
import tconstruct.library.tools.BowMaterial;
import tconstruct.library.tools.ToolMaterial;

import java.io.File;
import java.util.LinkedList;
import java.util.Objects;

import static tconstruct.library.TConstructRegistry.toolMaterials;

public class TICMaterialDumper extends WikiDumper {
    public TICMaterialDumper() {
        super("tools.dump.gtnhdumper.ticmaterial");
    }

    @Override
    public int getKeyIndex() {
        return 1;
    }

    @Override
    public String getKeyStr() {
        return "ticMaterials";
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
    public Iterable<Object[]> dumpObject(int mode) {
        LinkedList<Object[]> list = new LinkedList<>();
        for (int index : toolMaterials.keySet()) {
            ToolMaterial m = toolMaterials.get(index);
            ArrowMaterial arrowMaterial = TConstructRegistry.getArrowMaterial(index);
            BowMaterial bowMaterial = TConstructRegistry.getBowMaterial(index);
            Float mass = null;
            Float breakChance = null;
            Integer drawSpeed = null;
            Float flightSpeedMax = null;
            if (arrowMaterial != null) {
                mass = arrowMaterial.mass;
                breakChance = arrowMaterial.breakChance;
            }
            if (bowMaterial != null) {
                drawSpeed = bowMaterial.drawspeed;
                flightSpeedMax = bowMaterial.flightSpeedMax;
            }
            String ability = m.ability();
            if (m.stonebound > 0 && ability != "") {
                ability += " " + toRomaNumber((int) Math.abs(m.stonebound));
            }
            if (m.reinforced() > 0) {
                if (!Objects.equals(ability, "")) ability += ARRAY_SEPARATOR;
                ability += getReinforcedString(m.reinforced());
            }
            list.add(new Object[] {
                index,
                m.name(),
                m.prefixName(),
                m.durability(),
                m.handleDurability(),
                Math.round(m.durability() * m.handleDurability()),
                m.toolSpeed() / 100F,
                m.harvestLevel(),
                m.attack(),
                ability,
                drawSpeed,
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
