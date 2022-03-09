package com.veteam.voluminousenergy.recipe;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.veteam.voluminousenergy.blocks.blocks.VEBlocks;
import com.veteam.voluminousenergy.util.RecipeUtil;
import com.veteam.voluminousenergy.util.TagUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class ToolingRecipe extends VERecipe {
    public static final RecipeType<ToolingRecipe> RECIPE_TYPE = VERecipes.VERecipeTypes.TOOLING;

    public static final Serializer SERIALIZER = new Serializer();

    protected Lazy<ArrayList<Item>> bits;
    protected Lazy<ArrayList<Item>> basesAndBits;
    protected Lazy<ArrayList<Item>> bases;

    public final ResourceLocation recipeId;
    public Lazy<Ingredient> ingredient;
    public ItemStack result;

    protected boolean usesTagKey;
    protected String tagKeyString;

    private final Map<Ingredient, Integer> ingredients = new LinkedHashMap<>();

    public Map<Ingredient, Integer> getIngredientMap() {
        return ImmutableMap.copyOf(ingredients);
    }

    public ToolingRecipe(ResourceLocation recipeId){ this.recipeId = recipeId; }

    @Override
    public Ingredient getIngredient() {
        return ingredient.get();
    }

    public int getIngredientCount() {
        return ingredientCount;
    }

    @Override
    public ItemStack getResult() { return result; }

    @Override
    public boolean matches(Container inv, Level worldIn){
        ItemStack stack = inv.getItem(0);
        int count = stack.getCount();
        return ingredient.get().test(stack) && count >= ingredientCount;
    }

    @Override
    public ItemStack assemble(Container inv){
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height){
        return true;
    }

    @Override
    public ItemStack getResultItem(){
        return result;
    }

    @Override
    public ResourceLocation getId(){
        return recipeId;
    }

    @Override
    public RecipeSerializer<?> getSerializer(){
        return SERIALIZER;
    }

    @Override
    public RecipeType<?> getType(){
        return RECIPE_TYPE;
    }

    @Override
    public ItemStack getToastSymbol(){
        return new ItemStack(VEBlocks.TOOLING_STATION_BLOCK);
    }

    public ArrayList<Item> getBits(){
        return this.bits.get();
    }

    public ArrayList<Item> getBasesAndBits(){
        return this.basesAndBits.get();
    }

    public ArrayList<Item> getBases(){
        return this.bases.get();
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<ToolingRecipe>{

        @Override
        public ToolingRecipe fromJson(ResourceLocation recipeId, JsonObject json){

            ToolingRecipe recipe = new ToolingRecipe(recipeId);

            recipe.ingredient = Lazy.of(() -> Ingredient.fromJson(json.get("ingredient"))); // bits

            recipe.bits = Lazy.of(() -> {
                ArrayList<Item> items = new ArrayList<>();
                for (ItemStack item : recipe.ingredient.get().getItems()) {
                    items.add(item.getItem());
                }
                return items;
            });

            JsonObject toolBase = json.get("tool_base").getAsJsonObject();

            if(toolBase.has("tag") && !toolBase.has("item")){
                recipe.usesTagKey = true;
                ResourceLocation toolBaseResourceLocation = ResourceLocation.of(GsonHelper.getAsString(toolBase,"tag","minecraft:air"),':');
                recipe.tagKeyString = toolBaseResourceLocation.toString();

                recipe.bases = TagUtil.getLazyItems(toolBaseResourceLocation);
            } else if(!toolBase.has("tag") && toolBase.has("item")){
                recipe.usesTagKey = false;
                ResourceLocation secondInputResourceLocation = ResourceLocation.of(GsonHelper.getAsString(toolBase,"item","minecraft:air"),':');

                recipe.bases = Lazy.of(() -> {
                    ArrayList<Item> items = new ArrayList<>();
                    items.add((new ItemStack(ForgeRegistries.ITEMS.getValue(secondInputResourceLocation))).getItem());
                    return items;
                });
            } else {
                throw new JsonSyntaxException("Bad syntax for the Tooling Recipe");
            }

            // Create Anthology
            recipe.basesAndBits = RecipeUtil.createLazyAnthology(recipe.bases, recipe.bits);

            ResourceLocation itemResourceLocation = ResourceLocation.of(GsonHelper.getAsString(json.get("result").getAsJsonObject(), "item", "minecraft:air"),':');
            recipe.result = new ItemStack(ForgeRegistries.ITEMS.getValue(itemResourceLocation));

            return recipe;
        }

        @Nullable
        @Override
        public ToolingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer){
            ToolingRecipe recipe = new ToolingRecipe(recipeId);

            // Start with usesTagKey check
            recipe.usesTagKey = buffer.readBoolean();

            if (recipe.usesTagKey){
                recipe.tagKeyString = buffer.readComponent().getContents();
                ResourceLocation itemTagLocation = new ResourceLocation(recipe.tagKeyString);
                recipe.bases = TagUtil.getLazyItems(itemTagLocation);
            } else {
                int basesSize = buffer.readInt();
                for (int i = 0; i < basesSize; i++){
                    recipe.bases.get().add(buffer.readItem().getItem());
                }
            }

            int bitsSize = buffer.readInt();
            for (int i = 0; i < bitsSize; i++){
                recipe.bits.get().add(buffer.readItem().getItem());
            }

            recipe.basesAndBits = RecipeUtil.createLazyAnthology(recipe.bases, recipe.bits);

            recipe.result = buffer.readItem();
            recipe.ingredient = Lazy.of(() -> Ingredient.fromNetwork(buffer));
            return recipe;
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ToolingRecipe recipe){

            if (recipe.usesTagKey){
                buffer.writeComponent(new TextComponent(recipe.tagKeyString));
            } else { // does not use tags for item input
                buffer.writeInt(recipe.bases.get().size());
                recipe.bases.get().forEach(item -> {
                    buffer.writeItem(new ItemStack(item));
                });
            }

            buffer.writeInt(recipe.bits.get().size());
            recipe.bits.get().forEach(item -> {
                buffer.writeItem(new ItemStack(item));
            });

            buffer.writeItem(recipe.getResult());
            recipe.ingredient.get().toNetwork(buffer);
        }
    }
}
