package com.veteam.voluminousenergy.loot.AnimalFat;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class AnimalFatLootModifier extends LootModifier {
    private final Item itemAddition;
    private final int minAmount;
    private final int maxAmount;

    /**
     * Constructs a LootModifier.
     *
     * @param conditionsIn the ILootConditions that need to be matched before the loot is modified.
     */
    protected AnimalFatLootModifier(LootItemCondition[] conditionsIn, Item item, int minAmount, int maxAmount) {
        super(conditionsIn);
        this.itemAddition = item;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
    }

    @NotNull
    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
        float luck = context.getLuck() > 0 ? context.getLuck() : 1;
        float lootingModif = context.getLootingModifier() > 0 ? context.getLootingModifier() : 1;
        Random contextualizedRandom = context.getRandom();
        int amount = contextualizedRandom.nextInt(this.minAmount, this.maxAmount);
        amount = Math.round(amount * luck);
        amount = Math.round(amount * lootingModif);
        generatedLoot.add(new ItemStack(itemAddition, amount));
        System.out.println("Generated New Loot");
        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<AnimalFatLootModifier> {
        @Override
        public AnimalFatLootModifier read(ResourceLocation name, JsonObject jsonObject, LootItemCondition[] conditionsIn) {
            Item addition = ForgeRegistries.ITEMS.getValue(new ResourceLocation(GsonHelper.getAsString(jsonObject, "addition")));
            int minimumCount = GsonHelper.getAsInt(jsonObject, "minimum_count", 0);
            int maximumCount = GsonHelper.getAsInt(jsonObject, "maximum_count", 1);
            return new AnimalFatLootModifier(conditionsIn, addition, minimumCount, maximumCount);
        }

        @Override
        public JsonObject write(AnimalFatLootModifier instance) {
            JsonObject jsonObject = makeConditions(instance.conditions);
            jsonObject.addProperty("minimum_count", instance.minAmount);
            jsonObject.addProperty("maximum_count", instance.maxAmount);
            jsonObject.addProperty("addition", ForgeRegistries.ITEMS.getKey(instance.itemAddition).toString());
            return jsonObject;
        }
    }
}
