package com.veteam.voluminousenergy.recipe;

import com.veteam.voluminousenergy.blocks.blocks.VEBlocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

public class CentrifugalAgitatorRecipe extends VEFluidRecipe {
    public static final RecipeType<VEFluidRecipe> RECIPE_TYPE = VERecipes.VERecipeTypes.CENTRIFUGAL_AGITATING.get();

    @Override
    public @NotNull RecipeType<VEFluidRecipe> getType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull ItemStack getToastSymbol() {
        return new ItemStack(VEBlocks.CENTRIFUGAL_AGITATOR_BLOCK.get());
    }
}
