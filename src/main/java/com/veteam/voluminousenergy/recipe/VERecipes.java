package com.veteam.voluminousenergy.recipe;

import com.veteam.voluminousenergy.VoluminousEnergy;
import com.veteam.voluminousenergy.recipe.CombustionGenerator.CombustionGeneratorFuelRecipe;
import com.veteam.voluminousenergy.recipe.CombustionGenerator.CombustionGeneratorOxidizerRecipe;
import com.veteam.voluminousenergy.util.RecipeConstants;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.function.Supplier;

public class VERecipes {

    public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = create(ForgeRegistries.RECIPE_SERIALIZERS);

    private static <T extends IForgeRegistryEntry<T>> DeferredRegister<T> create(IForgeRegistry<T> registry) {
        return DeferredRegister.create(registry, VoluminousEnergy.MODID);
    }

    public static final class VERecipeTypes{
        public static final IRecipeType<PrimitiveBlastFurnaceRecipe> PRIMITIVE_BLAST_FURNACING = registerType(RecipeConstants.PRIMITIVE_BLAST_FURNACING);
        public static final IRecipeType<CrusherRecipe> CRUSHING = registerType(RecipeConstants.CRUSHING);
        public static final IRecipeType<ElectrolyzerRecipe> ELECTROLYZING = registerType(RecipeConstants.ELECTROLYZING);
        public static final IRecipeType<CentrifugalAgitatorRecipe> CENTRIFUGAL_AGITATING = registerType(RecipeConstants.CENTRIFUGAL_AGITATING);
        public static final IRecipeType<CompressorRecipe> COMPRESSING = registerType(RecipeConstants.COMPRESSING);
        public static final IRecipeType<StirlingGeneratorRecipe> STIRLING = registerType(RecipeConstants.STIRLING);
        public static final IRecipeType<CombustionGeneratorOxidizerRecipe> OXIDIZING = registerType(RecipeConstants.OXIDIZING);
        public static final IRecipeType<CombustionGeneratorFuelRecipe> FUEL_COMBUSTION = registerType(RecipeConstants.FUEL_COMBUSTION);
        public static final IRecipeType<AqueoulizerRecipe> AQUEOULIZING = registerType(RecipeConstants.AQUEOULIZING);
        public static final IRecipeType<DistillationRecipe> DISTILLING = registerType(RecipeConstants.DISTILLING);
    }

    public static final RegistryObject<IRecipeSerializer<?>> PRIMITIVE_BLAST_FURNACING = registerSerializer(RecipeConstants.PRIMITIVE_BLAST_FURNACING, PrimitiveBlastFurnaceRecipe.Serializer::new);
    public static final RegistryObject<IRecipeSerializer<?>> CRUSHING = registerSerializer(RecipeConstants.CRUSHING, CrusherRecipe.Serializer::new);
    public static final RegistryObject<IRecipeSerializer<?>> ELECTROLYZING = registerSerializer(RecipeConstants.ELECTROLYZING, ElectrolyzerRecipe.Serializer::new);
    public static final RegistryObject<IRecipeSerializer<?>> CENTRIFUGAL_AGITATING = registerSerializer(RecipeConstants.CENTRIFUGAL_AGITATING, CentrifugalAgitatorRecipe.Serializer::new);
    public static final RegistryObject<IRecipeSerializer<?>> COMPRESSING = registerSerializer(RecipeConstants.COMPRESSING, CompressorRecipe.Serializer::new);
    public static final RegistryObject<IRecipeSerializer<?>> STIRLING = registerSerializer(RecipeConstants.STIRLING, StirlingGeneratorRecipe.Serializer::new);
    public static final RegistryObject<IRecipeSerializer<?>> OXIDIZING = registerSerializer(RecipeConstants.OXIDIZING, CombustionGeneratorOxidizerRecipe.Serializer::new);
    public static final RegistryObject<IRecipeSerializer<?>> FUEL_COMBUSTION = registerSerializer(RecipeConstants.FUEL_COMBUSTION, CombustionGeneratorFuelRecipe.Serializer::new);
    public static final RegistryObject<IRecipeSerializer<?>> AQUEOULIZING = registerSerializer(RecipeConstants.AQUEOULIZING, AqueoulizerRecipe.Serializer::new);
    public static final RegistryObject<IRecipeSerializer<?>> DISTILLING = registerSerializer(RecipeConstants.DISTILLING, DistillationRecipe.Serializer::new);

    public static void init() {
        /*register types
        registerType(RecipeConstants.PRIMITIVE_BLAST_FURNACING,PrimitiveBlastFurnaceRecipe.RECIPE_TYPE);
        registerType(RecipeConstants.CRUSHING,CrusherRecipe.RECIPE_TYPE);
        registerType(RecipeConstants.ELECTROLYZING,ElectrolyzerRecipe.RECIPE_TYPE);
        registerType(RecipeConstants.CENTRIFUGAL_AGITATING, CentrifugalAgitatorRecipe.RECIPE_TYPE);
        registerType(RecipeConstants.COMPRESSING,CompressorRecipe.RECIPE_TYPE);
        registerType(RecipeConstants.STIRLING,StirlingGeneratorRecipe.RECIPE_TYPE);
        registerType(RecipeConstants.OXIDIZING,CombustionGeneratorOxidizerRecipe.RECIPE_TYPE);
        registerType(RecipeConstants.FUEL_COMBUSTION,CombustionGeneratorFuelRecipe.RECIPE_TYPE);
        registerType(RecipeConstants.AQUEOULIZING,AqueoulizerRecipe.RECIPE_TYPE);
        registerType(RecipeConstants.DISTILLING,DistillationRecipe.RECIPE_TYPE);

        //register serializers
        registerSerializer(RecipeConstants.PRIMITIVE_BLAST_FURNACING,PrimitiveBlastFurnaceRecipe.SERIALIZER);
        registerSerializer(RecipeConstants.CRUSHING,CrusherRecipe.SERIALIZER);
        registerSerializer(RecipeConstants.ELECTROLYZING,ElectrolyzerRecipe.SERIALIZER);
        registerSerializer(RecipeConstants.CENTRIFUGAL_AGITATING, CentrifugalAgitatorRecipe.SERIALIZER);
        registerSerializer(RecipeConstants.COMPRESSING,CompressorRecipe.SERIALIZER);
        registerSerializer(RecipeConstants.STIRLING,StirlingGeneratorRecipe.SERIALIZER);
        registerSerializer(RecipeConstants.OXIDIZING,CombustionGeneratorOxidizerRecipe.SERIALIZER);
        registerSerializer(RecipeConstants.FUEL_COMBUSTION,CombustionGeneratorFuelRecipe.SERIALIZER);
        registerSerializer(RecipeConstants.AQUEOULIZING,AqueoulizerRecipe.SERIALIZER);
        registerSerializer(RecipeConstants.DISTILLING,DistillationRecipe.SERIALIZER);
        */
    }



    private static RegistryObject<IRecipeSerializer<?>> registerSerializer(ResourceLocation name, Supplier<IRecipeSerializer<?>> serializer) {
        VoluminousEnergy.LOGGER.info("RSerializing: " + name.toString());
        return RECIPE_SERIALIZERS.register(name.getPath(), serializer);

        //IRecipeSerializer.register(name.toString(),serializer);
    }

    private static <T extends IRecipe<?>> IRecipeType<T> registerType(ResourceLocation name){
        VoluminousEnergy.LOGGER.info("RType: " + name.toString());
        return Registry.register(Registry.RECIPE_TYPE, name, new IRecipeType<T>() {
            @Override
            public String toString() {
                return name.toString();
            }
        });
        //Registry.register(Registry.RECIPE_TYPE, name, recipeType);
    }
}