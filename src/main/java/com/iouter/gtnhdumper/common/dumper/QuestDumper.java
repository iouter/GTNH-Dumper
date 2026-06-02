package com.iouter.gtnhdumper.common.dumper;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;

import com.iouter.gtnhdumper.common.base.WikiDumper;
import com.iouter.gtnhdumper.common.recipe.base.RecipeItem;
import com.iouter.gtnhdumper.common.utils.Utils;

import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.IDatabaseNBT;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestInstance;
import bq_standard.NbtBlockType;
import bq_standard.rewards.RewardChoice;
import bq_standard.rewards.RewardCommand;
import bq_standard.rewards.RewardItem;
import bq_standard.rewards.RewardQuestCompletion;
import bq_standard.rewards.RewardScoreboard;
import bq_standard.rewards.RewardXP;
import bq_standard.tasks.TaskBlockBreak;
import bq_standard.tasks.TaskCheckbox;
import bq_standard.tasks.TaskCrafting;
import bq_standard.tasks.TaskFluid;
import bq_standard.tasks.TaskHunt;
import bq_standard.tasks.TaskInteractEntity;
import bq_standard.tasks.TaskInteractItem;
import bq_standard.tasks.TaskLocation;
import bq_standard.tasks.TaskMeeting;
import bq_standard.tasks.TaskOptionalRetrieval;
import bq_standard.tasks.TaskRetrieval;
import bq_standard.tasks.TaskScoreboard;
import bq_standard.tasks.TaskXP;

public class QuestDumper extends WikiDumper {

    public QuestDumper() {
        super("tools.dump.gtnhdumper.quest");
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
        return new String[] { "uuid", "title", "description", "icon", "logic_quest", "logic_task", "repeat_time",
            "repeat_rel", "locked_progress", "auto_claim", "silent", "main", "global_share", "simultaneous",
            "visibility", "pre_requisites", "tasks", "rewards" };
    }

    @Override
    public Iterable<Object[]> dumpObject(int mode) {
        List<Object[]> list = new ArrayList<>();
        for (UUID uuid : QuestDatabase.INSTANCE.keySet()) {
            IQuest temp = QuestDatabase.INSTANCE.get(uuid);
            if (!(temp instanceof QuestInstance quest)) {
                continue;
            }
            String key = "betterquesting.quest." + Utils.uuidToBase64(uuid) + ".";

            RecipeItem icon = toRecipeItem(quest.getProperty(NativeProps.ICON));

            IDatabaseNBT<ITask, NBTTagList, NBTTagList> tasks = quest.getTasks();
            List<Map<String, Object>> taskList = new ArrayList<>();
            for (DBEntry<ITask> taskEntry : tasks.getEntries()) {
                ITask task = taskEntry.getValue();
                Map<String, Object> taskMap = getTask(task);
                if (taskMap != null) {
                    taskList.add(taskMap);
                }
            }
            IDatabaseNBT<IReward, NBTTagList, NBTTagList> rewards = quest.getRewards();
            List<Map<String, Object>> rewardList = new ArrayList<>();
            for (DBEntry<IReward> rewardEntry : rewards.getEntries()) {
                IReward reward = rewardEntry.getValue();
                Map<String, Object> rewardMap = getReward(reward);
                if (rewardMap != null) {
                    taskList.add(rewardMap);
                }
            }
            list.add(
                new Object[] { uuid, StatCollector.translateToLocal(key + "name"),
                    StatCollector.translateToLocal(key + "desc"), icon, quest.getProperty(NativeProps.LOGIC_QUEST)
                        .name()
                        .toLowerCase(),
                    quest.getProperty(NativeProps.LOGIC_TASK)
                        .name()
                        .toLowerCase(),
                    quest.getProperty(NativeProps.REPEAT_TIME), quest.getProperty(NativeProps.REPEAT_REL),
                    quest.getProperty(NativeProps.LOCKED_PROGRESS), quest.getProperty(NativeProps.AUTO_CLAIM),
                    quest.getProperty(NativeProps.SILENT), quest.getProperty(NativeProps.MAIN),
                    quest.getProperty(NativeProps.GLOBAL_SHARE), quest.getProperty(NativeProps.SIMULTANEOUS),
                    quest.getProperty(NativeProps.VISIBILITY)
                        .name()
                        .toLowerCase(),
                    getPreRequisites(quest), taskList, rewardList });
        }
        return list;
    }

    private Map<String, Set<UUID>> getPreRequisites(QuestInstance quest) {
        Map<String, Set<UUID>> map = new LinkedHashMap<>();
        Set<UUID> normal = new LinkedHashSet<>();
        Set<UUID> implicit = new LinkedHashSet<>();
        Set<UUID> hidden = new LinkedHashSet<>();
        for (UUID requisite : quest.getRequirements()) {
            IQuest.RequirementType type = quest.getRequirementType(requisite);
            switch (type) {
                case NORMAL: {
                    normal.add(requisite);
                    break;
                }
                case IMPLICIT: {
                    implicit.add(requisite);
                    break;
                }
                case HIDDEN: {
                    hidden.add(requisite);
                    break;
                }
            }
        }
        if (!normal.isEmpty()) {
            map.put("normal", normal);
        }
        if (!implicit.isEmpty()) {
            map.put("implicit", implicit);
        }
        if (!hidden.isEmpty()) {
            map.put("hidden", hidden);
        }
        if (map.isEmpty()) {
            return null;
        }
        return map;
    }

    private Map<String, Object> getTask(ITask task) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (task instanceof TaskXP xp) {
            map.put("type", "xp");
            map.put("amount", xp.amount);
            map.put("levels", xp.levels);
            map.put("consume", xp.consume);
        } else if (task instanceof TaskBlockBreak blockBreak) {
            map.put("type", "block_break");
            map.put(
                "blocks",
                blockBreak.blockTypes.stream()
                    .map(QuestDumper::toRecipeItem)
                    .toArray(RecipeItem[]::new));
        } else if (task instanceof TaskCheckbox) {
            map.put("type", "check_box");
        } else if (task instanceof TaskCrafting crafting) {
            map.put("type", "crafting");
            map.put(
                "required_items",
                crafting.requiredItems.stream()
                    .map(QuestDumper::toRecipeItem)
                    .toArray(RecipeItem[]::new));
            map.put("partial_match", crafting.partialMatch);
            map.put("ignore_nbt", crafting.ignoreNBT);
            map.put("allow_anvil", crafting.allowAnvil);
            map.put("allow_smelt", crafting.allowSmelt);
            map.put("allow_craft", crafting.allowCraft);
            map.put("allow_crafted_from_statistics", crafting.allowCraftedFromStatistics);
        } else if (task instanceof TaskFluid fluid) {
            map.put("type", "fluid");
            map.put("required_fluids", fluid.requiredFluids);
            map.put("ignore_nbt", fluid.ignoreNbt);
            map.put("consume", fluid.consume);
            map.put("group_detect", fluid.groupDetect);
            map.put("auto_consume", fluid.autoConsume);
        } else if (task instanceof TaskHunt hunt) {
            map.put("type", "hunt");
            map.put("id_name", hunt.idName);
            if (!hunt.damageType.isEmpty()) {
                map.put("damage_type", hunt.damageType);
            }
            map.put("required", hunt.required);
            map.put("ignore_nbt", hunt.ignoreNBT);
            map.put("subtypes", hunt.subtypes);
        } else if (task instanceof TaskInteractEntity interactEntity) {
            map.put("type", "interact_entity");
            if (interactEntity.targetItem != null) {
                map.put("target_item", toRecipeItem(interactEntity.targetItem));
            }
            map.put("ignore_item_nbt", interactEntity.ignoreItemNBT);
            map.put("partial_item_match", interactEntity.partialItemMatch);
            map.put("entity_id", interactEntity.entityID);
            if (!interactEntity.entityTags.func_150296_c()
                .isEmpty()) {
                map.put("entity_tags", interactEntity.entityTags);
            }
            map.put("entity_subtypes", interactEntity.entitySubtypes);
            map.put("ignore_entity_nbt", interactEntity.ignoreEntityNBT);
            map.put("on_interact", interactEntity.onInteract);
            map.put("on_hit", interactEntity.onHit);
            map.put("required", interactEntity.required);
        } else if (task instanceof TaskInteractItem interactItem) {
            map.put("type", "interact_item");
            if (interactItem.targetItem != null) {
                map.put("target_item", toRecipeItem(interactItem.targetItem));
            }
            if (interactItem.targetBlock.b != null) {
                map.put("target_block", toRecipeItem(interactItem.targetBlock));
            }
            map.put("partial_match", interactItem.partialMatch);
            map.put("ignore_nbt", interactItem.ignoreNBT);
            map.put("on_interact", interactItem.onInteract);
            map.put("on_hit", interactItem.onHit);
            map.put("required", interactItem.required);
        } else if (task instanceof TaskLocation location) {
            map.put("type", "location");
            map.put("name", location.name);
            if (!location.structure.isEmpty()) {
                map.put("structure", location.structure);
            }
            if (location.biome >= 0) {
                map.put("biome", location.biome);
            }
            map.put("x", location.x);
            map.put("y", location.y);
            map.put("z", location.z);
            map.put("dim", location.dim);
            if (location.range >= 0) {
                map.put("range", location.range);
            }
            map.put("visible", location.visible);
            map.put("hide_info", location.hideInfo);
            map.put("invert", location.invert);
            map.put("taxi_cab", location.taxiCab);
        } else if (task instanceof TaskMeeting meeting) {
            map.put("type", "meeting");
            map.put("id_name", meeting.idName);
            map.put("range", meeting.range);
            map.put("amount", meeting.amount);
            map.put("ignore_nbt", meeting.ignoreNBT);
            map.put("subtypes", meeting.subtypes);
            if (!meeting.targetTags.func_150296_c()
                .isEmpty()) {
                map.put("target_tags", meeting.targetTags);
            }
        } else if (task instanceof TaskOptionalRetrieval optionalRetrieval) {
            map.put("type", "optional_retrieval");
            map.put(
                "required_items",
                optionalRetrieval.requiredItems.stream()
                    .map(QuestDumper::toRecipeItem)
                    .toArray(RecipeItem[]::new));
            map.put("partial_match", optionalRetrieval.partialMatch);
            map.put("ignore_nbt", optionalRetrieval.ignoreNBT);
            map.put("consume", optionalRetrieval.consume);
            map.put("group_detect", optionalRetrieval.groupDetect);
            map.put("auto_consume", optionalRetrieval.autoConsume);
        } else if (task instanceof TaskRetrieval retrieval) {
            map.put("type", "retrieval");
            map.put(
                "required_items",
                retrieval.requiredItems.stream()
                    .map(QuestDumper::toRecipeItem)
                    .toArray(RecipeItem[]::new));
            map.put("partial_match", retrieval.partialMatch);
            map.put("ignore_nbt", retrieval.ignoreNBT);
            map.put("consume", retrieval.consume);
            map.put("group_detect", retrieval.groupDetect);
            map.put("auto_consume", retrieval.autoConsume);
        } else if (task instanceof TaskScoreboard scoreboard) {
            map.put("type", "scoreboard");
            map.put("score_name", scoreboard.scoreName);
            map.put("score_disp", scoreboard.scoreDisp);
            map.put("score_type", scoreboard.type);
            map.put("target", scoreboard.target);
            map.put("conversion", scoreboard.conversion);
            if (!scoreboard.suffix.isEmpty()) {
                map.put("suffix", scoreboard.suffix);
            }
            map.put(
                "operation",
                scoreboard.operation.name()
                    .toLowerCase());
        }
        if (map.isEmpty()) {
            return null;
        }
        return map;
    }

    private Map<String, Object> getReward(IReward reward) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (reward instanceof RewardChoice choice) {
            map.put("type", "choice");
            map.put(
                "choices",
                choice.choices.stream()
                    .map(QuestDumper::toRecipeItem)
                    .toArray(RecipeItem[]::new));
        } else if (reward instanceof RewardCommand command) {
            map.put("type", "command");
            map.put("command", command.command);
            map.put("hide_cmd", command.hideCmd);
            map.put("via_player", command.viaPlayer);
        } else if (reward instanceof RewardItem item) {
            map.put("type", "item");
            map.put(
                "items",
                item.items.stream()
                    .map(QuestDumper::toRecipeItem)
                    .toArray(RecipeItem[]::new));
        } else if (reward instanceof RewardQuestCompletion questCompletion) {
            map.put("type", "quest_completion");
            if (questCompletion.questNum != null) {
                map.put("quest_num", questCompletion.questNum);
            }
        } else if (reward instanceof RewardScoreboard scoreboard) {
            map.put("type", "scoreboard");
            map.put("score", scoreboard.score);
            map.put("score_type", scoreboard.type);
            map.put("relative", scoreboard.relative);
            map.put("value", scoreboard.value);
        } else if (reward instanceof RewardXP xp) {
            map.put("type", "xp");
            map.put("amount", xp.amount);
            map.put("levels", xp.levels);
        }
        if (map.isEmpty()) {
            return null;
        }
        return map;
    }

    public static RecipeItem toRecipeItem(NbtBlockType nbtBlockType) {
        Block block = nbtBlockType.b;
        int meta = nbtBlockType.m;
        if (meta < 0) {
            meta = 0;
        }
        int amount = nbtBlockType.n;
        NBTTagCompound nbt = nbtBlockType.tags;
        String oreDict = nbtBlockType.oreDict;
        if (oreDict != null) {
            return new RecipeItem("#" + oreDict, amount).withNBT(nbt.toString());
        }
        ItemStack stack = new ItemStack(block, amount, meta);
        return new RecipeItem(stack).withNBT(nbt.toString());
    }

    public static RecipeItem toRecipeItem(BigItemStack bigItemStack) {
        if (bigItemStack.hasOreDict()) {
            return new RecipeItem("#" + bigItemStack.getOreDict(), bigItemStack.stackSize);
        }
        return new RecipeItem(bigItemStack.getBaseStack()).withAmount(bigItemStack.stackSize);
    }

    @Override
    public ChatComponentTranslation dumpMessage(File file) {
        return new ChatComponentTranslation(
            "nei.options.tools.dump.gtnhdumper.quest.dumped",
            "dumps/" + file.getName());
    }
}
