package com.veteam.voluminousenergy.setup;

import com.veteam.voluminousenergy.blocks.blocks.VEBlocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class VESetup {

    public static ItemGroup itemGroup = new ItemGroup("voluminousenergy"){
        @Override
        public ItemStack makeIcon(){ return new ItemStack(VEBlocks.PRIMITIVE_BLAST_FURNACE_BLOCK); }
    };

    public void init(){}

}
