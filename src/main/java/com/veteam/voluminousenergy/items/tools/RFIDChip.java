package com.veteam.voluminousenergy.items.tools;

import com.veteam.voluminousenergy.persistence.ChunkFluid;
import com.veteam.voluminousenergy.persistence.ChunkFluids;
import com.veteam.voluminousenergy.setup.VESetup;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RFIDChip extends Item {

    public RFIDChip (){
        super(new Item.Properties()
                .stacksTo(16)
                .tab(VESetup.itemGroup)
                .rarity(Rarity.create("ELECTRONIC", ChatFormatting.GREEN))
        );
        setRegistryName("rfid_chip");
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @Nullable Level level, @NotNull List<Component> componentList, @NotNull TooltipFlag tooltipFlag) {
        CompoundTag tag = itemStack.getOrCreateTag();

        if(tag.contains("ve_x")) {

            int x = tag.getInt("ve_x");
            int z = tag.getInt("ve_z");

            ChunkFluid fluid = ChunkFluids.getInstance().getChunkFluid(new ChunkPos(x,z));
            if(fluid == null) {
                componentList.add(new TextComponent("Error chunk data is null / not saved!"));
            } else {
                componentList.add(new TextComponent(""));
                fluid.getFluids().forEach(f -> {
                    Component translatedComponent = new TranslatableComponent(f.getFluid().getAttributes().getTranslationKey());
                    String translatedString = translatedComponent.getString();
                    Component textComponent = new TextComponent(ChatFormatting.DARK_PURPLE + translatedString + ": " + ChatFormatting.LIGHT_PURPLE + f.getAmount());
                    componentList.add(textComponent);
                });
            }


            componentList.add(new TextComponent("Chunk X: " + x + " | Chunk Z: " + z));
        }
        super.appendHoverText(itemStack, level, componentList, tooltipFlag);
    }

}