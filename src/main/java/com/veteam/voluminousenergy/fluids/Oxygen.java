package com.veteam.voluminousenergy.fluids;

import com.veteam.voluminousenergy.VoluminousEnergy;
import com.veteam.voluminousenergy.fluids.flowingFluidBlocks.VEFlowingFluidBlock;
import com.veteam.voluminousenergy.setup.VESetup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public class Oxygen {
    public static final ResourceLocation OXYGEN_STILL_TEXTURE = new ResourceLocation(VoluminousEnergy.MODID,"/block/fluids/oxygen_still");
    public static final ResourceLocation OXYGEN_FLOWING_TEXTURE = new ResourceLocation(VoluminousEnergy.MODID,"/block/fluids/oxygen_flowing");

    public static Block.Properties stdProp = Block.Properties.of(Material.WATER).noCollission().strength(100.0F).noLootTable();

    public static FlowingFluid OXYGEN;
    public static FlowingFluid FLOWING_OXYGEN;
    public static VEFlowingFluidBlock OXYGEN_BLOCK;
    public static Item OXYGEN_BUCKET;

    public static FlowingFluid OxygenFluid(){
        OXYGEN = new VEFlowingGasFluid.Source(Oxygen.properties, 4);
        return OXYGEN;
    }

    public static FlowingFluid FlowingOxygenFluid(){
        FLOWING_OXYGEN = new VEFlowingGasFluid.Flowing(Oxygen.properties, 4);
        return FLOWING_OXYGEN;
    }

    public static VEFlowingFluidBlock FlowingOxygenBlock(){
        OXYGEN_BLOCK = new VEFlowingFluidBlock(() -> OXYGEN, stdProp);
        return OXYGEN_BLOCK;
    }

    public static Item OxygenBucket(){
        OXYGEN_BUCKET = new BucketItem(() -> OXYGEN, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).tab(VESetup.itemGroup));
        return OXYGEN_BUCKET;
    }

    public static final FluidType OXYGEN_FLUID_TYPE = new FluidType(FluidType.Properties.create()
            .adjacentPathType(BlockPathTypes.WATER)
            .canConvertToSource(false)
            .canDrown(false)
            .canExtinguish(false)
            .canHydrate(false)
            .canPushEntity(true)
            .canConvertToSource(false)
            .canSwim(false)
            .lightLevel(0)
            .density(1)
            .temperature(300)
            .viscosity(1)
            .motionScale(0.75)
            .fallDistanceModifier(0)
            .rarity(Rarity.COMMON)
            .supportsBoating(false)
            //.sound(,)
    );

    public static final ForgeFlowingFluid.Properties properties = new ForgeFlowingFluid.Properties(() -> OXYGEN_FLUID_TYPE, () -> OXYGEN, () -> FLOWING_OXYGEN)
            .block(() -> OXYGEN_BLOCK).bucket(() -> OXYGEN_BUCKET);
}