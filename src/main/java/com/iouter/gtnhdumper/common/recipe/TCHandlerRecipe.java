package com.iouter.gtnhdumper.common.recipe;

import codechicken.nei.NEIServerUtils;
import codechicken.nei.recipe.GuiRecipeTab;
import codechicken.nei.recipe.HandlerInfo;
import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.RecipeCatalysts;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.google.common.base.Objects;
import com.iouter.gtnhdumper.Utils;
import com.iouter.gtnhdumper.common.recipe.base.RecipeItem;
import com.iouter.gtnhdumper.common.recipe.base.RecipeUtil;
import com.iouter.gtnhdumper.common.recipe.base.TCRecipe;
import net.glease.tc4tweak.api.infusionrecipe.EnhancedInfusionRecipe;
import net.glease.tc4tweak.api.infusionrecipe.InfusionRecipeExt;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import ru.timeconqueror.tcneiadditions.util.TCUtil;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.crafting.ShapelessArcaneRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TCHandlerRecipe {
    private final String name;
    private final String identifier;
    private final String source;
    private final String markedItem;
    private final ArrayList<String> catalysts;
    private ArrayList<TCRecipe> recipes;

    public TCHandlerRecipe(IRecipeHandler handler) {
        this.name = handler.getRecipeName();
        this.catalysts = new ArrayList<>();

        final String handlerName = handler.getHandlerId();
        final String handlerId = Objects.firstNonNull(
            handler instanceof TemplateRecipeHandler ? ((TemplateRecipeHandler) handler).getOverlayIdentifier()
                : null,
            "null");

        this.identifier = handlerId;
        this.source = handlerName;

        HandlerInfo info = GuiRecipeTab.getHandlerInfo(handlerName, handlerId);
        final ItemStack markedItemStack = info != null ? info.getItemStack() : null;
        this.markedItem = markedItemStack != null ? Utils.getItemKeyWithNBT(markedItemStack) : "null";

        RecipeCatalysts.getRecipeCatalysts(handler).stream().forEach(positionedStack -> {
            ItemStack[] items = positionedStack.items;
            if (items == null)
                return;
            for (ItemStack stack : items) {
                catalysts.add(Utils.getItemKeyWithNBT(stack));
            }
        });
        this.recipes = new ArrayList<>();
        final String clazz = Utils.getAfterLastDot(handler.getHandlerId()).replace(".", "_");
        if (clazz.equals("AspectCombinationHandler")) {
            for (Aspect aspect : Aspect.getCompoundAspects()) {
                Aspect[] components = aspect.getComponents();
                if (components == null)
                    continue;
                AspectList inputAspects = new AspectList();
                for (Aspect iA : components) {
                    inputAspects.add(iA, 1);
                }
                AspectList outputAspects = new AspectList().add(aspect, 1);
                recipes.add(new TCRecipe().withInputAspects(inputAspects).withOutputAspects(outputAspects));
            }
        } else if (clazz.equals("ArcaneCraftingShapedHandler")) {
            for (Object o : ThaumcraftApi.getCraftingRecipes()) {
                if (o instanceof ShapedArcaneRecipe) {
                    ShapedArcaneRecipe tcRecipe = (ShapedArcaneRecipe) o;
                    Object[] inputItems = new Object[9];
                    for (int i = 0; i < tcRecipe.getInput().length; i++) {
                        Object oInput = tcRecipe.getInput()[i];
                        if (isItem(oInput)) {
                            inputItems[i] = getItemFromObject(oInput);
                        }
                    }
                    recipes.add(new TCRecipe()
                        .withInputItems(inputItems)
                        .withInputAspects(tcRecipe.aspects)
                        .withOutputItems(new RecipeItem(tcRecipe.getRecipeOutput()))
                        .withResearch(tcRecipe.getResearch())
                    );
                }
            }
        } else if (clazz.equals("ArcaneCraftingShapelessHandler")) {
            for (Object o : ThaumcraftApi.getCraftingRecipes()) {
                if (o instanceof ShapelessArcaneRecipe) {
                    ShapelessArcaneRecipe tcRecipe = (ShapelessArcaneRecipe) o;
                    recipes.add(new TCRecipe().withInputItems(Arrays
                            .stream(tcRecipe.getInput().toArray())
                            .filter(TCHandlerRecipe::isItem)
                            .map(TCHandlerRecipe::getItemFromObject)
                            .toArray(Object[]::new))
                        .withInputAspects(tcRecipe.getAspects())
                        .withOutputItems(new RecipeItem(tcRecipe.getRecipeOutput()))
                        .withResearch(tcRecipe.getResearch()));
                }
            }
        } else if (clazz.equals("TCNACrucibleRecipeHandler")) {
            for (Object o : ThaumcraftApi.getCraftingRecipes()) {
                if (o instanceof CrucibleRecipe) {
                    CrucibleRecipe tcRecipe = (CrucibleRecipe) o;
                    recipes.add(new TCRecipe()
                        .withInputItems(getItemFromObject(tcRecipe.catalyst))
                        .withInputAspects(tcRecipe.aspects)
                        .withOutputItems(new RecipeItem(tcRecipe.getRecipeOutput()))
                        .withResearch(tcRecipe.key)
                    );
                }
            }
        } else if (clazz.equals("TCNAInfusionRecipeHandler")) {
            for (Object o : ThaumcraftApi.getCraftingRecipes()) {
                if (o instanceof InfusionRecipe) {
                    InfusionRecipe tcRecipe = (InfusionRecipe) o;
                    try {
                        EnhancedInfusionRecipe r = InfusionRecipeExt.get().convert(tcRecipe);
                        ItemStack outputStack;
                        if (tcRecipe.getRecipeOutput() instanceof ItemStack) {
                            outputStack = TCUtil.getAssociatedItemStack(tcRecipe.getRecipeOutput());
                        } else {
                            ItemStack temp = TCUtil.getAssociatedItemStack(tcRecipe.getRecipeOutput());
                            if (temp == null)
                                continue;
                            else
                                outputStack = temp.copy();
                            Object[] obj = (Object[]) tcRecipe.getRecipeOutput();
                            NBTBase tag = (NBTBase) obj[1];
                            outputStack.setTagInfo((String) obj[0], tag);
                        }
                        recipes.add(new TCRecipe()
                            .withKeyItem(getItemFromObject(r.getCentral().getRepresentativeStacks()))
                            .withInputItems(r
                                .getComponentsExt()
                                .stream()
                                .map(rI -> getItemFromObject(rI.getRepresentativeStacks()))
                                .filter(java.util.Objects::nonNull)
                                .toArray(Object[]::new))
                            .withInputAspects(tcRecipe.getAspects())
                            .withOutputItems(getItemFromObject(outputStack))
                            .withInstability(tcRecipe.getInstability())
                            .withResearch(tcRecipe.getResearch())
                        );
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        if (this.recipes.isEmpty()) {
            this.recipes = null;
        }
    }

    public static boolean isItem(Object o) {
        return (o instanceof ItemStack || o instanceof ItemStack[]
            || o instanceof String
            || (o instanceof List && !((List<?>) o).isEmpty()));
    }

    public static Object getItemFromObject(Object o) {
        ItemStack[] stacks = NEIServerUtils.extractRecipeItems(o);
        return RecipeUtil.getRecipeItems(stacks);
    }

}
