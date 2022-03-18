package com.veteam.voluminousenergy.compat.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import com.veteam.voluminousenergy.VoluminousEnergy;
import com.veteam.voluminousenergy.blocks.blocks.VEBlocks;
import com.veteam.voluminousenergy.compat.jei.VoluminousEnergyPlugin;
import com.veteam.voluminousenergy.recipe.ImplosionCompressorRecipe;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ImplosionCompressionCategory implements IRecipeCategory<ImplosionCompressorRecipe> {

    private final IDrawable background;
    private IDrawable icon;
    private IDrawable slotDrawable;
    private IDrawable arrow;
    private IDrawable emptyArrow;

    public ImplosionCompressionCategory(IGuiHelper guiHelper){
        // 68, 12 | 40, 65 -> 10 px added for chance
        ResourceLocation GUI = new ResourceLocation(VoluminousEnergy.MODID, "textures/gui/jei/jei.png");
        background = guiHelper.drawableBuilder(GUI, 68, 12, 70, 40).build();
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(VEBlocks.IMPLOSION_COMPRESSOR_BLOCK));
        slotDrawable = guiHelper.getSlotDrawable();
        arrow = guiHelper.drawableBuilder(GUI, 176, 0, 23, 17).build();
        emptyArrow = guiHelper.drawableBuilder(GUI,199,0,23,17).buildAnimated(200, IDrawableAnimated.StartDirection.LEFT, true);
    }

    @Override
    public @NotNull RecipeType getRecipeType(){
        return new RecipeType(VoluminousEnergyPlugin.IMPLOSION_COMPRESSION_UID, ImplosionCompressorRecipe.class);
    }

    @Deprecated
    @Override
    public ResourceLocation getUid(){
        return VoluminousEnergyPlugin.IMPLOSION_COMPRESSION_UID;
    }

    @Deprecated
    @Override
    public Class<? extends ImplosionCompressorRecipe> getRecipeClass() {
        return ImplosionCompressorRecipe.class;
    }

    @Override
    public Component getTitle() {
        return TextUtil.translateString("jei.voluminousenergy.implosion_compressing");
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
    public void draw(ImplosionCompressorRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack matrixStack, double mouseX, double mouseY) {
        arrow.draw(matrixStack,24, 12);
        emptyArrow.draw(matrixStack,24,12);
        slotDrawable.draw(matrixStack,2,1);
        slotDrawable.draw(matrixStack,2,19);
        slotDrawable.draw(matrixStack,48,10);
    }

    public void ingredientHandler(ImplosionCompressorRecipe recipe,
                                  IIngredientAcceptor itemInputAcceptor,
                                  IIngredientAcceptor explosiveInputAcceptor,
                                  IIngredientAcceptor itemOutputAcceptor) {
        // Input
        ArrayList<ItemStack> inputStacks = new ArrayList<>();
        for (ItemStack itemStack : recipe.ingredient.get().getItems()){
            itemStack.setCount(recipe.getIngredientCount());
            inputStacks.add(itemStack);
        }

        itemInputAcceptor.addIngredients(VanillaTypes.ITEM, inputStacks);

        // Explosive input
        explosiveInputAcceptor.addIngredient(VanillaTypes.ITEM, new ItemStack(Items.GUNPOWDER, recipe.ingredientCount));

        // Output
        ItemStack resultStack = recipe.result;
        resultStack.setCount(recipe.getOutputAmount());
        itemOutputAcceptor.addIngredient(VanillaTypes.ITEM, resultStack);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder recipeLayout, ImplosionCompressorRecipe recipe, IFocusGroup focusGroup) {
        // Input
        IRecipeSlotBuilder itemInput = recipeLayout.addSlot(RecipeIngredientRole.INPUT, 3, 2);
        IRecipeSlotBuilder explosiveInput = recipeLayout.addSlot(RecipeIngredientRole.INPUT, 3, 20);

        // Output
        IRecipeSlotBuilder itemOutput = recipeLayout.addSlot(RecipeIngredientRole.OUTPUT, 49, 11);

        itemInput.setSlotName(TextUtil.TRANSLATED_INPUT_SLOT.getString());
        explosiveInput.setSlotName(TextUtil.TRANSLATED_INPUT_SLOT.getString());
        itemOutput.setSlotName(TextUtil.TRANSLATED_OUTPUT_SLOT.getString());

        this.ingredientHandler(recipe, itemInput, explosiveInput, itemOutput);
    }
}