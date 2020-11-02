package com.veteam.voluminousenergy.recipe;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.gson.JsonObject;
import com.veteam.voluminousenergy.util.RecipeConstants;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AqueoulizerRecipe extends VEFluidRecipe {
    public static final IRecipeType<VEFluidRecipe> RECIPE_TYPE = new IRecipeType<VEFluidRecipe>() {
        @Override
        public String toString() {
            return RecipeConstants.AQUEOULIZING.toString();
        }
    };

    public static final Serializer SERIALIZER = new Serializer();

    public static ArrayList<Item> ingredientList = new ArrayList<>();

    public static ArrayList<Item> fluidInputList = new ArrayList<>();

    private final ResourceLocation recipeId;
    private int processTime;

    public ItemStack inputFluid;
    public ItemStack result;
    private int inputAmount;
    private int outputAmount;

    public AqueoulizerRecipe() {
        recipeId = null;
    }

    public AqueoulizerRecipe(ResourceLocation recipeId){
        this.recipeId = recipeId;
    }

    @Override
    public Ingredient getIngredient(){ return ingredient;}

    @Override
    public int getIngredientCount(){ return ingredientCount;}

    @Override
    public ItemStack getResult() {return result;}

    @Override
    public FluidStack getOutputFluid(){
        if (result.getItem() instanceof BucketItem){
            return new FluidStack(((BucketItem) result.getItem()).getFluid(), outputAmount);
        }
        return FluidStack.EMPTY;
    }

    @Override
    public List<Integer> getAmounts() {
        return null;
    }

    public FluidStack getInputFluid(){
        if (inputFluid.getItem() instanceof BucketItem){
            return new FluidStack(((BucketItem) inputFluid.getItem()).getFluid(), inputAmount);
        }
        return FluidStack.EMPTY;
    }

    @Override
    public boolean matches(IInventory inv, World worldIn){
        ItemStack stack = inv.getStackInSlot(0);
        int count = stack.getCount();
        return ingredient.test(stack) && count >= ingredientCount;
    }

    @Override
    public ItemStack getCraftingResult(IInventory inv){return ItemStack.EMPTY;}

    @Override
    public boolean canFit(int width, int height){return true;}

    @Override
    public ItemStack getRecipeOutput(){return result;}

    @Override
    public ResourceLocation getId(){return recipeId;}

    @Override
    public IRecipeSerializer<?> getSerializer(){ return SERIALIZER;}

    @Override
    public IRecipeType<VEFluidRecipe> getType(){return RECIPE_TYPE;}

    @Override
    public ArrayList<Item> getIngredientList() {
        return null;
    }

    @Override
    public List<FluidStack> getFluids() {
        List<FluidStack> f = new ArrayList<>();
        f.add(getInputFluid());
        return f;
    }

    @Override
    public List<ItemStack> getResults() {
        return null;
    }

    @Override
    public int getOutputAmount() {return outputAmount;}

    @Override
    public int getProcessTime() { return processTime; }

    @Override
    public int getInputAmount(){
        return inputAmount;
    }





    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<AqueoulizerRecipe> {
        @Override
        public AqueoulizerRecipe read(ResourceLocation recipeId, JsonObject json) {
            AqueoulizerRecipe recipe = new AqueoulizerRecipe(recipeId);

            recipe.ingredient = Ingredient.deserialize(json.get("ingredient"));
            recipe.ingredientCount = JSONUtils.getInt(json.get("ingredient").getAsJsonObject(), "count", 1);
            recipe.processTime = JSONUtils.getInt(json,"process_time",200);

            for (ItemStack stack : recipe.ingredient.getMatchingStacks()){
                if(!ingredientList.contains(stack.getItem())){
                    ingredientList.add(stack.getItem());
                }
            }

            ResourceLocation bucketResourceLocation = ResourceLocation.create(JSONUtils.getString(json.get("input_fluid").getAsJsonObject(),"item","minecraft:empty"),':');
            int inputFluidAmount = JSONUtils.getInt(json.get("input_fluid").getAsJsonObject(),"amount",0);
            recipe.inputFluid = new ItemStack(ForgeRegistries.ITEMS.getValue(bucketResourceLocation));
            recipe.inputAmount = inputFluidAmount;

            ResourceLocation secondBucketResourceLocation = ResourceLocation.create(JSONUtils.getString(json.get("result").getAsJsonObject(),"item","minecraft:empty"),':');
            int secondFluidAmount = JSONUtils.getInt(json.get("result").getAsJsonObject(),"amount",0);
            recipe.result = new ItemStack(ForgeRegistries.ITEMS.getValue(secondBucketResourceLocation));
            recipe.outputAmount = secondFluidAmount;

            if (!fluidInputList.contains(recipe.inputFluid.getItem())){
                fluidInputList.add(recipe.inputFluid.getItem());
            }

            return recipe;
        }

        @Nullable
        @Override
        public AqueoulizerRecipe read(ResourceLocation recipeId, PacketBuffer buffer){
            AqueoulizerRecipe recipe = new AqueoulizerRecipe((recipeId));
            recipe.ingredient = Ingredient.read(buffer);
            recipe.ingredientCount = buffer.readByte();
            recipe.result = buffer.readItemStack();
            recipe.inputFluid = buffer.readItemStack();
            recipe.inputAmount = buffer.readInt();
            recipe.processTime = buffer.readInt();
            recipe.outputAmount = buffer.readInt();
            return recipe;
        }

        @Override
        public void write(PacketBuffer buffer, AqueoulizerRecipe recipe){
            recipe.ingredient.write(buffer);
            buffer.writeByte(recipe.getIngredientCount());
            buffer.writeItemStack(recipe.getResult());
            buffer.writeItemStack(recipe.inputFluid);
            buffer.writeInt(recipe.inputAmount);
            buffer.writeInt(recipe.processTime);
            buffer.writeInt(recipe.outputAmount);
        }
    }
}