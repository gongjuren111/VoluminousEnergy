package com.veteam.voluminousenergy.recipe;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.veteam.voluminousenergy.blocks.blocks.VEBlocks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public class CentrifugalSeparatorRecipe extends VERecipe{

    public static final RecipeType<CentrifugalSeparatorRecipe> RECIPE_TYPE = VERecipes.VERecipeTypes.CENTRIFUGAL_SEPARATION.get();

    public static final CentrifugalSeparatorRecipe.Serializer SERIALIZER = new CentrifugalSeparatorRecipe.Serializer();

    public final ResourceLocation recipeId;
    public Lazy<Ingredient> ingredient;
    public int ingredientCount;
    public ItemStack result;
    private ItemStack rngResult0;
    private ItemStack rngResult1;
    private ItemStack rngResult2;
    private int processTime;
    private int outputAmount;
    private int outputRngAmount0;
    private int outputRngAmount1;
    private int outputRngAmount2;
    private float chance0;
    private float chance1;
    private float chance2;
    private int usesBucket;

    private final Map<Ingredient, Integer> ingredients = new LinkedHashMap<>();

    public CentrifugalSeparatorRecipe(ResourceLocation recipeId){
        this.recipeId = recipeId;
    }

    public Ingredient getIngredient(){ return ingredient.get();}

    public int getIngredientCount(){ return ingredientCount;}

    public ItemStack getResult() {return result;}

    public ItemStack getRngItemSlot0(){return rngResult0;}

    public ItemStack getRngItemSlot1(){return rngResult1;}

    public ItemStack getRngItemSlot2(){return rngResult2;}

    public float getChance0(){return chance0;}

    public float getChance1(){return chance1;}

    public float getChance2(){return chance2;}

    @Override
    public boolean matches(Container inv, Level worldIn){
        ItemStack stack = inv.getItem(0);
        int count = stack.getCount();
        return ingredient.get().test(stack) && count >= ingredientCount;
    }

    @Override
    public ItemStack getResultItem(){return result;}

    @Override
    public ResourceLocation getId(){return recipeId;}

    @Override
    public RecipeSerializer<?> getSerializer(){ return SERIALIZER;}

    @Override
    public RecipeType<?> getType(){return RECIPE_TYPE;}

    public int getOutputAmount() {return outputAmount;}

    public int getOutputRngAmount0(){return outputRngAmount0;}

    public int getOutputRngAmount1(){return outputRngAmount1;}

    public int getOutputRngAmount2(){return outputRngAmount2;}

    public int getProcessTime() { return processTime; }

    public int needsBuckets() {return usesBucket;}

    public Map<Ingredient, Integer> getIngredientMap() {
        return ImmutableMap.copyOf(ingredients);
    }

    @Override
    public ItemStack getToastSymbol(){
        return new ItemStack(VEBlocks.CENTRIFUGAL_SEPARATOR_BLOCK.get());
    }

    public static class Serializer implements RecipeSerializer<CentrifugalSeparatorRecipe>{

        @Override
        public @NotNull CentrifugalSeparatorRecipe fromJson(@NotNull ResourceLocation recipeId, JsonObject json){
            CentrifugalSeparatorRecipe recipe = new CentrifugalSeparatorRecipe(recipeId);

            JsonObject ingredientJson = json.get("ingredient").getAsJsonObject();

            recipe.ingredient = Lazy.of(() -> Ingredient.fromJson(ingredientJson));
            recipe.ingredientCount = GsonHelper.getAsInt(ingredientJson, "count", 1);
            recipe.processTime = GsonHelper.getAsInt(json,"process_time",200);

            // Main Output Slot
            ResourceLocation itemResourceLocation = ResourceLocation.of(GsonHelper.getAsString(json.get("result").getAsJsonObject(),"item","minecraft:air"),':');
            int itemAmount = GsonHelper.getAsInt(json.get("result").getAsJsonObject(),"count",1);
            int bucketNeeded = GsonHelper.getAsInt(json.get("result").getAsJsonObject(),"buckets",0);
            recipe.result = new ItemStack(ForgeRegistries.ITEMS.getValue(itemResourceLocation));
            recipe.outputAmount = itemAmount;
            recipe.usesBucket = bucketNeeded;

            // First RNG Slot, RNG 0
            ResourceLocation rngResourceLocation0 = ResourceLocation.of(GsonHelper.getAsString(json.get("rng_slot_0").getAsJsonObject(),"item","minecraft:air"),':');
            int rngAmount0 = GsonHelper.getAsInt(json.get("rng_slot_0").getAsJsonObject(),"count",0);
            float rngChance0 = GsonHelper.getAsFloat(json.get("rng_slot_0").getAsJsonObject(),"chance",0); //Enter % as DECIMAL. Ie 50% = 0.5

            recipe.rngResult0 = new ItemStack(ForgeRegistries.ITEMS.getValue(rngResourceLocation0));
            recipe.outputRngAmount0 = rngAmount0;
            recipe.chance0 = rngChance0;

            //Second RNG Slot, RNG 1
            ResourceLocation rngResourceLocation1 = ResourceLocation.of(GsonHelper.getAsString(json.get("rng_slot_1").getAsJsonObject(),"item","minecraft:air"),':');
            int rngAmount1 = GsonHelper.getAsInt(json.get("rng_slot_1").getAsJsonObject(),"count",0);
            float rngChance1 = GsonHelper.getAsFloat(json.get("rng_slot_1").getAsJsonObject(),"chance",0); //Enter % as DECIMAL. Ie 50% = 0.5

            recipe.rngResult1 = new ItemStack(ForgeRegistries.ITEMS.getValue(rngResourceLocation1));
            recipe.outputRngAmount1 = rngAmount1;
            recipe.chance1 = rngChance1;

            //Third RNG Slot, RNG 2
            ResourceLocation rngResourceLocation2 = ResourceLocation.of(GsonHelper.getAsString(json.get("rng_slot_2").getAsJsonObject(),"item","minecraft:air"),':');
            int rngAmount2 = GsonHelper.getAsInt(json.get("rng_slot_2").getAsJsonObject(),"count",0);
            float rngChance2 = GsonHelper.getAsFloat(json.get("rng_slot_2").getAsJsonObject(),"chance",0); //Enter % as DECIMAL. Ie 50% = 0.5

            recipe.rngResult2 = new ItemStack(ForgeRegistries.ITEMS.getValue(rngResourceLocation2));
            recipe.outputRngAmount2 = rngAmount2;
            recipe.chance2 = rngChance2;

            return recipe;
        }

        @Nullable
        @Override
        public CentrifugalSeparatorRecipe fromNetwork(@NotNull ResourceLocation recipeId, FriendlyByteBuf buffer){
            CentrifugalSeparatorRecipe recipe = new CentrifugalSeparatorRecipe(recipeId);
            recipe.ingredientCount = buffer.readByte();
            recipe.result = buffer.readItem();
            recipe.processTime = buffer.readInt();
            recipe.outputAmount = buffer.readInt();
            recipe.usesBucket = buffer.readInt();
            //RNG 0
            recipe.rngResult0 = buffer.readItem();
            recipe.outputRngAmount0 = buffer.readInt();
            recipe.chance0 = buffer.readFloat();
            //RNG 1
            recipe.rngResult1 = buffer.readItem();
            recipe.outputRngAmount1 = buffer.readInt();
            recipe.chance1 = buffer.readFloat();
            //RNG 2
            recipe.rngResult2 = buffer.readItem();
            recipe.outputRngAmount2 = buffer.readInt();
            recipe.chance2 = buffer.readFloat();

            // Lazies
            Ingredient tempIngredient = Ingredient.fromNetwork(buffer);
            recipe.ingredient = Lazy.of(() -> tempIngredient);

            return recipe;
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, CentrifugalSeparatorRecipe recipe){
            buffer.writeByte(recipe.getIngredientCount());
            buffer.writeItem(recipe.getResult());
            buffer.writeInt(recipe.processTime);
            buffer.writeInt(recipe.outputAmount);
            buffer.writeInt(recipe.usesBucket);
            //RNG 0
            buffer.writeItem(recipe.rngResult0);
            buffer.writeInt(recipe.outputRngAmount0);
            buffer.writeFloat(recipe.chance0);
            //RNG 1
            buffer.writeItem(recipe.rngResult1);
            buffer.writeInt(recipe.outputRngAmount1);
            buffer.writeFloat(recipe.chance1);
            //RNG 2
            buffer.writeItem(recipe.rngResult2);
            buffer.writeInt(recipe.outputRngAmount2);
            buffer.writeFloat(recipe.chance2);

            // Lazies
            recipe.ingredient.get().toNetwork(buffer);
        }
    }

}
