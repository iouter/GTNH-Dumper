package com.iouter.gtnhdumper.common;

import codechicken.nei.config.DataDumper;
import codechicken.nei.config.HandlerDumper;
import codechicken.nei.recipe.GuiRecipeTab;
import codechicken.nei.recipe.GuiUsageRecipe;
import codechicken.nei.recipe.HandlerInfo;
import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.google.common.base.Objects;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iouter.gtnhdumper.CommonProxy;
import com.iouter.gtnhdumper.Utils;
import com.iouter.gtnhdumper.common.recipe.GTDefaultHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.GeneralHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.TCHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.base.RecipeItem;
import com.iouter.gtnhdumper.common.recipe.serializer.AspectListSerializer;
import com.iouter.gtnhdumper.common.recipe.serializer.AspectSerializer;
import com.iouter.gtnhdumper.common.recipe.serializer.ElementSerializer;
import com.iouter.gtnhdumper.common.recipe.serializer.FluidStackSerializer;
import com.iouter.gtnhdumper.common.recipe.serializer.ItemStackSerializer;
import com.iouter.gtnhdumper.common.recipe.serializer.MaterialsSerializer;
import com.iouter.gtnhdumper.common.recipe.serializer.RecipeItemSerializer;
import com.iouter.gtnhdumper.common.recipe.ShapedCraftingHandlerRecipe;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import gregtech.api.enums.Element;
import gregtech.api.enums.Materials;
import gregtech.nei.GTNEIDefaultHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.fluids.FluidStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RecipesDumper extends DataDumper {
    public RecipesDumper() {
        super("tools.dump.gtnhdumper.recipe");
    }

    private static Object dumpRecipes(IRecipeHandler recipeHandler) {
        final String clazz = Utils.getAfterLastDot(recipeHandler.getHandlerId()).replace(".", "_");
        if (CommonProxy.isGTLoaded) {
            if (recipeHandler instanceof GTNEIDefaultHandler) {
                GTNEIDefaultHandler gtDefaultHandler = (GTNEIDefaultHandler) recipeHandler;
                return new GTDefaultHandlerRecipe(gtDefaultHandler);
            }
        }
        if (CommonProxy.isTCNEIAdditionsLoaded) {
            if (clazz.equals("AspectCombinationHandler") ||
                clazz.equals("ArcaneCraftingShapedHandler") ||
                clazz.equals("ArcaneCraftingShapelessHandler") ||
                clazz.equals("TCNACrucibleRecipeHandler") ||
                clazz.equals("TCNAInfusionRecipeHandler")) {
                return new TCHandlerRecipe(recipeHandler);
            }
        }
        if (clazz.contains("Shaped")) {
            return new ShapedCraftingHandlerRecipe(recipeHandler);
        }
        return new GeneralHandlerRecipe(recipeHandler);
    }

    @Override
    public String[] header() {
        return new String[]{"Recipe json"};
    }

    @Override
    public Iterable<String[]> dump(int mode) {
        List<String[]> recipesList = new ArrayList<>();
        for (IRecipeHandler handler : GuiUsageRecipe.usagehandlers) {
            final String name = handler.getRecipeName();
            recipesList.add(new String[]{handler.getRecipeName()});
            final String handlerName = handler.getHandlerId();
            final String handlerId = Objects.firstNonNull(
                handler instanceof TemplateRecipeHandler ? ((TemplateRecipeHandler) handler).getOverlayIdentifier()
                    : null,
                "null");
            HandlerInfo info = GuiRecipeTab.getHandlerInfo(handlerName, handlerId);
            String modID = info != null ? info.getModId() : "Unknown";
            String id = Utils.getAfterLastDot(handlerId);
            String clazz = Utils.getAfterLastDot(handlerName);
            File file = new File( "dumps/recipes/" + modID + "/" + clazz + "_" + id + ".json");
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(RecipeItem.class, new RecipeItemSerializer())
                .registerTypeAdapter(Element.class, new ElementSerializer())
                .registerTypeAdapter(Materials.class, new MaterialsSerializer())
                .registerTypeAdapter(ItemStack.class, new ItemStackSerializer())
                .registerTypeAdapter(FluidStack.class, new FluidStackSerializer())
                .registerTypeAdapter(Aspect.class, new AspectSerializer())
                .registerTypeAdapter(AspectList.class, new AspectListSerializer())
                .create();
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(dumpRecipes(handler), writer);
                System.out.println("已写入：" + file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("导出" + name + "时发生错误：" + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
        return recipesList;
    }

    @Override
    public ChatComponentTranslation dumpMessage(File file) {
        return new ChatComponentTranslation(
            "nei.options.tools.dump.gtnhdumper.recipes.dumped", "dumps/" + file.getName());
    }

    @Override
    public int modeCount() {
        return 1;
    }
}
