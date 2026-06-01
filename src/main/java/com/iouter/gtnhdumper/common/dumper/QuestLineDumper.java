package com.iouter.gtnhdumper.common.dumper;

import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuestLine;
import betterquesting.questing.QuestLine;
import betterquesting.questing.QuestLineDatabase;
import com.iouter.gtnhdumper.common.base.WikiDumper;
import com.iouter.gtnhdumper.common.utils.Utils;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuestLineDumper extends WikiDumper {
    public QuestLineDumper() {
        super("tools.dump.gtnhdumper.questline");
    }

    @Override
    public int getKeyIndex() {
        return 0;
    }

    @Override
    public String getKeyStr() {
        return "uuid";
    }

    @Override
    public String[] header() {
        return new String[] {
            "uuid",
            "icon",
            "visibility",
            "title",
            "description",
            "quests"
        };
    }

    @Override
    public Iterable<Object[]> dumpObject(int mode) {
        List<Object[]> list = new ArrayList<>();

        for (UUID uuid : QuestLineDatabase.INSTANCE.keySet()) {
            IQuestLine iQuestLine = QuestLineDatabase.INSTANCE.get(uuid);
            if (!(iQuestLine instanceof QuestLine)) {
                continue;
            }
            QuestLine questLine = (QuestLine) iQuestLine;
            String key = "betterquesting.questline." + Utils.uuidToBase64(uuid) + ".";
            list.add(new Object[]{
                uuid,
                QuestDumper.toRecipeItem(questLine.getProperty(NativeProps.ICON)),
                questLine.getProperty(NativeProps.VISIBILITY),
                StatCollector.translateToLocal(key + "name"),
                StatCollector.translateToLocal(key + "desc"),
                questLine.keySet()
            });
        }

        return list;
    }

    @Override
    public ChatComponentTranslation dumpMessage(File file) {
        return new ChatComponentTranslation(
            "nei.options.tools.dump.gtnhdumper.questline.dumped", "dumps/" + file.getName());
    }
}
