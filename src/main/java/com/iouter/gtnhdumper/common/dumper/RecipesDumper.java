package com.iouter.gtnhdumper.common.dumper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.bdew.neiaddons.forestry.BaseBreedingRecipeHandler;
import net.bdew.neiaddons.forestry.BaseProduceRecipeHandler;
import net.minecraft.util.ChatComponentTranslation;

import com.google.common.base.Objects;
import com.gtnewhorizons.aspectrecipeindex.nei.AlchemyRecipeHandler;
import com.gtnewhorizons.aspectrecipeindex.nei.AspectCombinationHandler;
import com.gtnewhorizons.aspectrecipeindex.nei.InfusionRecipeHandler;
import com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench.ShapedArcaneRecipeHandler;
import com.gtnewhorizons.aspectrecipeindex.nei.arcaneworkbench.ShapelessArcaneRecipeHandler;
import com.iouter.gtnhdumper.CommonProxy;
import com.iouter.gtnhdumper.GTNHDumper;
import com.iouter.gtnhdumper.common.recipe.AvaExtremeShapedHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.ForestryHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.GTDefaultHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.GasSiphonHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.GeneralHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.MobHandlerInfernalRecipe;
import com.iouter.gtnhdumper.common.recipe.MobHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.ShapedCraftingHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.SpacePumpModuleHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.TCHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.base.BaseHandlerRecipe;
import com.iouter.gtnhdumper.common.utils.Utils;
import com.kuba6000.mobsinfo.nei.MobHandler;
import com.kuba6000.mobsinfo.nei.MobHandlerInfernal;

import codechicken.nei.config.DataDumper;
import codechicken.nei.recipe.GuiRecipeTab;
import codechicken.nei.recipe.GuiUsageRecipe;
import codechicken.nei.recipe.HandlerInfo;
import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;
import fox.spiteful.avaritia.compat.nei.ExtremeShapedRecipeHandler;
import gregtech.nei.GTNEIDefaultHandler;
import gtnhintergalactic.nei.GasSiphonRecipeHandler;
import gtnhintergalactic.nei.SpacePumpModuleRecipeHandler;

public class RecipesDumper extends DataDumper {

    public RecipesDumper() {
        super("tools.dump.gtnhdumper.recipe");
    }

    private static BaseHandlerRecipe dumpRecipes(IRecipeHandler recipeHandler) {
        final String clazz = Utils.getAfterLastDot(recipeHandler.getHandlerId())
            .replace(".", "_");
        if (CommonProxy.isGTLoaded) {
            if (recipeHandler instanceof GasSiphonRecipeHandler) {
                return new GasSiphonHandlerRecipe(recipeHandler);
            }
            if (recipeHandler instanceof SpacePumpModuleRecipeHandler) {
                return new SpacePumpModuleHandlerRecipe(recipeHandler);
            }
            if (recipeHandler instanceof GTNEIDefaultHandler) {
                return new GTDefaultHandlerRecipe((GTNEIDefaultHandler) recipeHandler);
            }
        }
        if (CommonProxy.isTCLoaded) {
            if (recipeHandler instanceof AspectCombinationHandler || recipeHandler instanceof ShapedArcaneRecipeHandler
                || recipeHandler instanceof ShapelessArcaneRecipeHandler
                || recipeHandler instanceof AlchemyRecipeHandler
                || recipeHandler instanceof InfusionRecipeHandler) {
                return new TCHandlerRecipe(recipeHandler);
            }
        }
        if (CommonProxy.isAvaritiaLoaded) {
            if (recipeHandler instanceof ExtremeShapedRecipeHandler) {
                return new AvaExtremeShapedHandlerRecipe(recipeHandler);
            }
        }
        if (CommonProxy.isFRLoaded) {
            if (CommonProxy.isNEIAddonLoaded) {
                if (recipeHandler instanceof BaseBreedingRecipeHandler
                    || recipeHandler instanceof BaseProduceRecipeHandler) {
                    return new ForestryHandlerRecipe(recipeHandler);
                }
            }
        }
        if (CommonProxy.isMobsInfoLoaded) {
            if (recipeHandler instanceof MobHandler) {
                return new MobHandlerRecipe((MobHandler) recipeHandler);
            }
            if (recipeHandler instanceof MobHandlerInfernal) {
                return new MobHandlerInfernalRecipe((MobHandlerInfernal) recipeHandler);
            }
        }
        if (clazz.contains("Shaped") && !clazz.equals("RecipeHandlerRollingMachineShaped")) {
            return new ShapedCraftingHandlerRecipe(recipeHandler);
        }
        return new GeneralHandlerRecipe(recipeHandler);
    }

    @Override
    public String[] header() {
        return new String[] { "Recipe json" };
    }

    @Override
    public Iterable<String[]> dump(int mode) {
        List<String[]> recipesList = new ArrayList<>();
        for (IRecipeHandler handler : GuiUsageRecipe.usagehandlers) {
            final String name = handler.getRecipeName();
            recipesList.add(new String[] { handler.getRecipeName() });
            final String handlerName = handler.getHandlerId();
            final String handlerId = Objects
                .firstNonNull(handler instanceof TemplateRecipeHandler ? handler.getOverlayIdentifier() : null, "null");
            HandlerInfo info = GuiRecipeTab.getHandlerInfo(handlerName, handlerId);
            String modID = info != null ? info.getModId() : "Unknown";
            String id = Utils.getAfterLastDot(handlerId);
            String clazz = Utils.getAfterLastDot(handlerName);
            String fileName = "dumps/recipes/" + modID + "/" + clazz + "_" + id + ".json";
            fileName = Utils.replacePathIllegalChars(fileName);
            File file = new File(fileName);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            try (FileWriter writer = new FileWriter(file)) {
                GTNHDumper.GSON.toJson(dumpRecipes(handler).build(), writer);
                GTNHDumper.info("已写入：" + file.getAbsolutePath());
            } catch (IOException e) {
                file.deleteOnExit();
                GTNHDumper.LOG.error(e);
            } catch (Exception e) {
                GTNHDumper.info("导出" + name + "时发生错误：" + e.getLocalizedMessage());
                file.deleteOnExit();
                GTNHDumper.LOG.error(e);
            }
        }
        return recipesList;
    }

    @Override
    public ChatComponentTranslation dumpMessage(File file) {
        return new ChatComponentTranslation(
            "nei.options.tools.dump.gtnhdumper.recipes.dumped",
            "dumps/" + file.getName());
    }

    @Override
    public int modeCount() {
        return 1;
    }
}
