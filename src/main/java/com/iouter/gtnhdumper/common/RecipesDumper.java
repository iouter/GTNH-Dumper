package com.iouter.gtnhdumper.common;

import codechicken.nei.config.DataDumper;
import codechicken.nei.recipe.GuiUsageRecipe;
import codechicken.nei.recipe.IRecipeHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iouter.gtnhdumper.CommonProxy;
import com.iouter.gtnhdumper.Utils;
import com.iouter.gtnhdumper.common.recipe.GTDefaultHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.GeneralHandlerRecipe;
import com.iouter.gtnhdumper.common.recipe.base.RecipeItem;
import com.iouter.gtnhdumper.common.recipe.serializer.ElementSerializer;
import com.iouter.gtnhdumper.common.recipe.serializer.FluidStackSerializer;
import com.iouter.gtnhdumper.common.recipe.serializer.ItemStackSerializer;
import com.iouter.gtnhdumper.common.recipe.serializer.MaterialsSerializer;
import com.iouter.gtnhdumper.common.recipe.serializer.RecipeItemSerializer;
import com.iouter.gtnhdumper.common.recipe.ShapedCraftingHandlerRecipe;
import gregtech.api.enums.Element;
import gregtech.api.enums.Materials;
import gregtech.nei.GTNEIDefaultHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.fluids.FluidStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecipesDumper extends DataDumper {
    public RecipesDumper() {
        super("tools.dump.gtnhdumper.recipes");
    }

    private static Object dumpRecipes(IRecipeHandler recipeHandler) {
        String name = recipeHandler.getRecipeName();
        if (CommonProxy.isGTLoaded) {
            if (recipeHandler instanceof GTNEIDefaultHandler) {
                GTNEIDefaultHandler gtDefaultHandler = (GTNEIDefaultHandler) recipeHandler;
                return new GTDefaultHandlerRecipe(gtDefaultHandler);
            }
//            else if (recipeHandler instanceof GTNEIImprintHandler) {
//
//            }
        }
        if (name.equals("有序合成")) {
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
            final String clazz = Utils.getAfterLastDot(handler.getHandlerId()).replace(".", "_");
            File file = new File( "dumps/recipes/" + clazz + "_" + name + ".json");
            Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(RecipeItem.class, new RecipeItemSerializer())
                .registerTypeAdapter(Element.class, new ElementSerializer())
                .registerTypeAdapter(Materials.class, new MaterialsSerializer())
                .registerTypeAdapter(ItemStack.class, new ItemStackSerializer())
                .registerTypeAdapter(FluidStack.class, new FluidStackSerializer())
                .create();
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(dumpRecipes(handler), writer);
                System.out.println("已写入：" + file.getAbsolutePath());
            } catch (IOException e) {
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

//    private static
//
//    private static Gson dumpGTDefaultRecipes(GTNEIDefaultHandler defaultHandler) {
//        defaultHandler.getRecipeMap();
//    }
}
