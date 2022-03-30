package com.veteam.voluminousenergy.compat.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import com.veteam.voluminousenergy.blocks.blocks.VEBlocks;
import com.veteam.voluminousenergy.blocks.screens.CrusherScreen;
import com.veteam.voluminousenergy.blocks.screens.VEContainerScreen;
import com.veteam.voluminousenergy.compat.jei.VoluminousEnergyPlugin;
import com.veteam.voluminousenergy.recipe.CrusherRecipe;
import com.veteam.voluminousenergy.util.TextUtil;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class CrushingCategory implements IRecipeCategory<CrusherRecipe> {

    private final IDrawable background;
    private IDrawable icon;
    private IDrawable slotDrawable;
    private IDrawable arrow;
    public static final RecipeType RECIPE_TYPE = new RecipeType(VoluminousEnergyPlugin.CRUSHING_UID, CrusherRecipe.class);

    public CrushingCategory(IGuiHelper guiHelper){
        // 68, 12 | 40, 65 -> 10 px added for chance
        background = guiHelper.drawableBuilder(CrusherScreen.getGUI(), 68, 12, 40, 70).build();
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(VEBlocks.CRUSHER_BLOCK));
        slotDrawable = guiHelper.getSlotDrawable();
        arrow = guiHelper.drawableBuilder(CrusherScreen.getGUI(), 176, 0, 17, 24).buildAnimated(200, IDrawableAnimated.StartDirection.TOP, false);
    }

    @Override
    public @NotNull RecipeType getRecipeType(){
        return RECIPE_TYPE;
    }

    @Deprecated
    @Override
    public ResourceLocation getUid(){
        return VoluminousEnergyPlugin.CRUSHING_UID;
    }

    @Deprecated
    @Override
    public Class<? extends CrusherRecipe> getRecipeClass() {
        return CrusherRecipe.class;
    }

    @Override
    public Component getTitle() {
        return TextUtil.translateString("jei.voluminousenergy.crushing");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void draw(CrusherRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack matrixStack, double mouseX, double mouseY) {
        arrow.draw(matrixStack,10, 19);


        if (recipe.getRngItem() != null && recipe.getRngItem().getItem() != Items.AIR){ // Check RNG if it's not air
            int chance = (int)(recipe.getChance()*100);
            int xPos = 20;
            if (chance < 100 && chance >= 10){
                xPos += 3;
            } else if (chance < 10){
                xPos += 5;
            }
            Minecraft.getInstance().font.draw(matrixStack,chance + "%",xPos,65, VEContainerScreen.GREY_TEXT_COLOUR);
        }

    }

    public void ingredientHandler(CrusherRecipe recipe,
                                  IIngredientAcceptor itemInputAcceptor,
                                  IIngredientAcceptor itemOutputAcceptor,
                                  IIngredientAcceptor itemRNGOutputAcceptor) {
        // Input
        ArrayList<ItemStack> inputStacks = new ArrayList<>();
        for (ItemStack itemStack : recipe.ingredient.get().getItems()){
            itemStack.setCount(recipe.ingredientCount);
            inputStacks.add(itemStack);
        }
        itemInputAcceptor.addIngredients(VanillaTypes.ITEM, inputStacks);

        // Output
        ItemStack resultStack = recipe.result.copy();
        resultStack.setCount(recipe.getOutputAmount());
        itemOutputAcceptor.addIngredient(VanillaTypes.ITEM, resultStack);

        ItemStack rngStack = recipe.rngResult.copy();
        rngStack.setCount(recipe.getOutputRngAmount());
        itemRNGOutputAcceptor.addIngredient(VanillaTypes.ITEM, rngStack);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder recipeLayout, CrusherRecipe recipe, IFocusGroup focusGroup) {
        // Input
        IRecipeSlotBuilder itemInput = recipeLayout.addSlot(RecipeIngredientRole.INPUT, 12, 1);

        // Output
        IRecipeSlotBuilder primaryItemOutput = recipeLayout.addSlot(RecipeIngredientRole.OUTPUT, 3, 46);
        IRecipeSlotBuilder rngItemOutput = recipeLayout.addSlot(RecipeIngredientRole.OUTPUT,  21, 46);

        itemInput.setSlotName(TextUtil.TRANSLATED_INPUT_SLOT.getString());
        primaryItemOutput.setSlotName(TextUtil.TRANSLATED_OUTPUT_SLOT.getString());
        rngItemOutput.setSlotName(TextUtil.TRANSLATED_RNG_SLOT.getString());

        this.ingredientHandler(recipe, itemInput, primaryItemOutput, rngItemOutput);
    }

}