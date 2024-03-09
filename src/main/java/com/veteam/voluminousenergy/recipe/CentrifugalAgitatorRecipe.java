package com.veteam.voluminousenergy.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.veteam.voluminousenergy.blocks.blocks.VEBlocks;
import com.veteam.voluminousenergy.recipe.parser.RecipeParser;
import com.veteam.voluminousenergy.recipe.serializer.FluidSerializerHelper;
import com.veteam.voluminousenergy.util.recipe.VERecipeCodecs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CentrifugalAgitatorRecipe extends VERecipe {
    public static final RecipeType<VERecipe> RECIPE_TYPE = VERecipes.VERecipeTypes.CENTRIFUGAL_AGITATING.get();

    private final RecipeParser parser = RecipeParser.forRecipe(this)
            .addFluidIngredient(0,0)
            .addFluidResult(1,0)
            .addFluidResult(2,1);

    public CentrifugalAgitatorRecipe() {

    }

    public CentrifugalAgitatorRecipe(List<VERecipeCodecs.RegistryFluidIngredient> fi, List<FluidStack> of, int processTime) {
        super(List.of(), fi, of, List.of(), processTime);
    }

    public static final RecipeSerializer<VERecipe> SERIALIZER = new RecipeSerializer<>() {

        public static final Codec<VERecipe> VE_RECIPE_CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                VERecipeCodecs.VE_FLUID_INGREDIENT_CODEC.listOf().fieldOf("fluid_ingredients").forGetter((getter) -> getter.registryFluidIngredients),
                VERecipeCodecs.VE_OUTPUT_FLUID_CODEC.listOf().fieldOf("fluid_results").forGetter((getter) -> getter.fluidOutputList),
                Codec.INT.fieldOf("process_time").forGetter((getter) -> getter.processTime)
        ).apply(instance, CentrifugalAgitatorRecipe::new));

        private static final FluidSerializerHelper<VERecipe> helper = new FluidSerializerHelper<>();

        @Nullable
        @Override
        public VERecipe fromNetwork(@NotNull FriendlyByteBuf buffer) {
            return helper.fromNetwork(new CentrifugalAgitatorRecipe(), buffer);
        }

        @Override
        public @NotNull Codec<VERecipe> codec() {
            return VE_RECIPE_CODEC;
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull VERecipe recipe) {
            helper.toNetwork(buffer, recipe);
        }
    };
    @Override
    public @NotNull RecipeSerializer<? extends VERecipe> getSerializer(){ return SERIALIZER;}
    @Override
    public @NotNull RecipeType<VERecipe> getType() {
        return RECIPE_TYPE;
    }

    @Override
    public @NotNull ItemStack getToastSymbol() {
        return new ItemStack(VEBlocks.CENTRIFUGAL_AGITATOR_BLOCK.get());
    }

    @Override
    public RecipeParser getParser() {
        return parser;
    }
}
