package com.veteam.voluminousenergy.blocks.tiles;

import com.veteam.voluminousenergy.blocks.blocks.VEBlocks;
import com.veteam.voluminousenergy.blocks.containers.GasFiredFurnaceContainer;
import com.veteam.voluminousenergy.items.VEItems;
import com.veteam.voluminousenergy.recipe.CombustionGenerator.CombustionGeneratorFuelRecipe;
import com.veteam.voluminousenergy.recipe.VEFluidRecipe;
import com.veteam.voluminousenergy.tools.sidemanager.VESlotManager;
import com.veteam.voluminousenergy.util.*;
import com.veteam.voluminousenergy.util.recipe.RecipeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class GasFiredFurnaceTile extends VEFluidTileEntity implements IVECountable {

    public VESlotManager bucketInputSm = new VESlotManager(0,Direction.UP,true, SlotType.INPUT);
    public VESlotManager bucketOutputSm = new VESlotManager(1, Direction.DOWN, true,SlotType.OUTPUT);
    public VESlotManager furnaceInputSm = new VESlotManager(2, Direction.EAST, true,SlotType.INPUT);
    public VESlotManager furnaceOutputSm = new VESlotManager(3, Direction.WEST,true,SlotType.OUTPUT);

    List<VESlotManager> slotManagers =  new ArrayList<>() {{
        add(bucketInputSm);
        add(bucketOutputSm);
        add(furnaceInputSm);
        add(furnaceOutputSm);
    }};

    RelationalTank fuelTank = new RelationalTank(new FluidTank(TANK_CAPACITY),0,null,null, TankType.INPUT,"fuel_tank:fuel_tank_gui");

    List<RelationalTank> fluidManagers = new ArrayList<>() {{
        add(fuelTank);
    }};

    private int fuelCounter;
    private int fuelLength;

    private final AtomicReference<ItemStack> inputItemStack = new AtomicReference<ItemStack>(new ItemStack(Items.AIR,0));
    private final AtomicReference<ItemStack> referenceStack = new AtomicReference<ItemStack>(new ItemStack(Items.AIR,0));

    public GasFiredFurnaceTile(BlockPos pos, BlockState state) {
        super(VEBlocks.GAS_FIRED_FURNACE_TILE.get(), pos, state);
        this.fuelTank.setValidFluids(RecipeUtil.getCombustibleFuelsWithoutLevel());
    }

    public ItemStackHandler inventory = createHandler();

    @Override
    public @Nonnull ItemStackHandler getInventoryHandler() {
        return inventory;
    }

    @NotNull
    @Override
    public List<VESlotManager> getSlotManagers() {
        return slotManagers;
    }

    @Override
    public void tick() {
        updateClients();
        ItemStack bucketInput = inventory.getStackInSlot(0).copy();
        ItemStack bucketOutput = inventory.getStackInSlot(1).copy();
        ItemStack furnaceInput = inventory.getStackInSlot(2).copy();
        ItemStack furnaceOutput = inventory.getStackInSlot(3).copy();

        fuelTank.setInput(bucketInput.copy());
        fuelTank.setOutput(bucketOutput.copy());

        if(this.inputFluid(fuelTank,0,1)) return;
        if(this.outputFluid(fuelTank,0,1)) return;

        inputItemStack.set(furnaceInput.copy()); // Atomic Reference, use this to query recipes FOR OUTPUT SLOT

        // Main Processing occurs here
        if (fuelTank.getTank() != null && !fuelTank.getTank().isEmpty()) {
            SmeltingRecipe furnaceRecipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(furnaceInput.copy()), level).orElse(null);
            BlastingRecipe blastingRecipe = level.getRecipeManager().getRecipeFor(RecipeType.BLASTING, new SimpleContainer(furnaceInput.copy()), level).orElse(null);

            if ((furnaceRecipe != null || blastingRecipe != null) && countChecker(furnaceRecipe,blastingRecipe,furnaceOutput.copy()) && itemChecker(furnaceRecipe,blastingRecipe,furnaceOutput.copy())){
                if (counter == 1) {
                    //LOGGER.debug("What is in the output slot? " + furnaceOutput);
                    // Extract item
                    inventory.extractItem(2,1,false);

                    // Set output based on recipe
                    ItemStack newOutputStack;
                    if (furnaceRecipe != null) {
                        newOutputStack = furnaceRecipe.getResultItem(level.registryAccess()).copy();
                    } else {
                        newOutputStack = blastingRecipe.getResultItem(level.registryAccess()).copy();
                    }
                    //LOGGER.debug("NewOutputStack: " + newOutputStack);

                    // Output Item
                    if (furnaceOutput.getItem() != newOutputStack.getItem() || furnaceOutput.getItem() == Items.AIR) {
                        //LOGGER.debug("The output is not equal to the new output Stack");
                        if(furnaceOutput.getItem() == Items.AIR){ // Fix air >1 jamming slots
                            furnaceOutput.setCount(1);
                        }
                        if (furnaceRecipe != null){
                            newOutputStack.setCount(furnaceRecipe.getResultItem(level.registryAccess()).getCount());
                        } else {
                            newOutputStack.setCount(blastingRecipe.getResultItem(level.registryAccess()).getCount());
                        }
                        //LOGGER.debug("About to insert in pt1: " + newOutputStack);
                        inventory.insertItem(3, newOutputStack.copy(),false); // CRASH the game if this is not empty!

                    } else { // Assuming the recipe output item is already in the output slot
                        // Simply change the stack to equal the output amount
                        if (furnaceRecipe != null){
                            furnaceOutput.setCount(furnaceRecipe.getResultItem(level.registryAccess()).getCount());
                        } else {
                            furnaceOutput.setCount(blastingRecipe.getResultItem(level.registryAccess()).getCount());
                        }
                        //LOGGER.debug("About to insert in pt2: " + furnaceOutput);
                        inventory.insertItem(3, furnaceOutput.copy(),false); // Place the new output stack on top of the old one
                    }

                    counter--;
                    this.setChanged();
                } else if (counter > 0) {
                    counter--;
                } else {
                    counter = this.calculateCounter(200, inventory.getStackInSlot(4));
                    length = counter;
                    this.referenceStack.set(furnaceInput.copy());
                }

                // Fuel Management
                if (fuelCounter == 1){
                    fuelCounter--;
                } else if (fuelCounter > 0){
                    fuelCounter--;
                } else {
                    VEFluidRecipe recipe = null; //= RecipeUtil.getFuelCombustionRecipe(level, fuelTank.getTank().getFluid().copy());// TODO FIX ME
                    if (recipe != null){
                        // Drain Input
                        fuelTank.getTank().drain(250, IFluidHandler.FluidAction.EXECUTE);
                        fuelCounter = recipe.getProcessTime()/4;
                        if(inventory.getStackInSlot(4).getCount() > 0 && inventory.getStackInSlot(4).getItem() == VEItems.QUARTZ_MULTIPLIER.get()){
                            fuelCounter = fuelCounter/(inventory.getStackInSlot(4).getCount()^2);
                        } else if (!inventory.getStackInSlot(4).isEmpty() && TagUtil.isTaggedMachineUpgradeItem(inventory.getStackInSlot(4))){
                            ItemStack upgradeStack = inventory.getStackInSlot(4).copy();
                            if (upgradeStack.getTag() != null && !upgradeStack.getTag().isEmpty()){
                                float multiplier = upgradeStack.getTag().getFloat("multiplier");
                                multiplier = multiplier / 0.5F > 1 ? 1 : multiplier / 0.5F;
                                fuelCounter = (int)((float)(fuelCounter * multiplier));
                            }
                        }
                        fuelLength = fuelCounter;
                        this.setChanged();
                    }
                }

            } else counter = 0;


        } else counter = 0;
    }

    @Override
    public void load(CompoundTag tag) {
        fuelCounter = tag.getInt("fuel_counter");
        fuelLength = tag.getInt("fuel_length");
        super.load(tag);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        tag.putInt("fuel_counter", fuelCounter);
        tag.putInt("fuel_length", fuelLength);
        super.saveAdditional(tag);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
        super.onDataPacket(net, pkt);
    }


    private ItemStackHandler createHandler() {
        return new ItemStackHandler(5) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) { //IS ITEM VALID PLEASE DO THIS PER SLOT TO SAVE DEBUG HOURS!!!!
                if (slot == 0 || slot == 1){
                    return level.getRecipeManager().getRecipeFor(CombustionGeneratorFuelRecipe.RECIPE_TYPE, new SimpleContainer(stack),level).orElse(null) != null
                            || stack.getItem() == Items.BUCKET;
                } else if (slot == 2) {
                    return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(stack), level).orElse(null) != null
                            || level.getRecipeManager().getRecipeFor(RecipeType.BLASTING, new SimpleContainer(stack), level).orElse(null) != null;
                } else if (slot == 3) {
                    SmeltingRecipe furnaceRecipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(inputItemStack.get()), level).orElse(null);
                    BlastingRecipe blastingRecipe = level.getRecipeManager().getRecipeFor(RecipeType.BLASTING, new SimpleContainer(inputItemStack.get()), level).orElse(null);

                    // If both recipes are null, then don't bother
                    if (blastingRecipe == null && furnaceRecipe == null) return false;

                    if (furnaceRecipe != null) {
                        return stack.getItem() == furnaceRecipe.getResultItem(level.registryAccess()).getItem();
                    }

                    return stack.getItem() == blastingRecipe.getResultItem(level.registryAccess()).getItem();
                } else if (slot == 4){
                    return TagUtil.isTaggedMachineUpgradeItem(stack);
                }
                return false;
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) { //ALSO DO THIS PER SLOT BASIS TO SAVE DEBUG HOURS!!!

                if (slot == 0 || slot == 1) {
                    return super.insertItem(slot, stack, simulate);
                }

                if (slot == 2){
                    ItemStack referenceStack = stack.copy();
                    referenceStack.setCount(64);
                    SmeltingRecipe recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(referenceStack), level).orElse(null);
                    BlastingRecipe blastingRecipe = level.getRecipeManager().getRecipeFor(RecipeType.BLASTING, new SimpleContainer(referenceStack),level).orElse(null);

                    if (recipe != null || blastingRecipe != null){
                        return super.insertItem(slot, stack, simulate);
                    }

                } else if (slot == 3){
                    return super.insertItem(slot, stack, simulate);
                } else if (slot == 4 && TagUtil.isTaggedMachineUpgradeItem(stack)){
                    return super.insertItem(slot, stack, simulate);
                }
                return stack;
            }

            @Override
            @Nonnull
            public ItemStack extractItem(int slot, int amount, boolean simulate){
                if (level != null && !simulate){
                    SmeltingRecipe furnaceRecipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(referenceStack.get()), level).orElse(null);
                    BlastingRecipe blastingRecipe = level.getRecipeManager().getRecipeFor(RecipeType.BLASTING, new SimpleContainer(referenceStack.get()), level).orElse(null);
                    if(blastingRecipe != null) {
                        if (inventory.getStackInSlot(slot).getItem() == blastingRecipe.getResultItem(level.registryAccess()).getItem()) {
                            if(blastingRecipe.getExperience() > 0){
                                generateXP(amount, blastingRecipe.getExperience());
                            }
                        }
                    } else if (furnaceRecipe != null) {
                        if (inventory.getStackInSlot(slot).getItem() == furnaceRecipe.getResultItem(level.registryAccess()).getItem()) {
                            if (furnaceRecipe.getExperience() > 0) {
                                generateXP(amount, furnaceRecipe.getExperience());
                            }
                        }
                    }
                }
                return super.extractItem(slot,amount,simulate);
            }
        };
    }

    private void generateXP(int craftedAmount, float experience){
        if(level == null) return;
        int i = Mth.floor((float)craftedAmount * experience);
        float f = Mth.frac((float)craftedAmount * experience);
        if (f != 0.0F && Math.random() < (double)f) ++i;

        while(i > 0) {
            int j = ExperienceOrb.getExperienceValue(i);
            i -= j;
            level.addFreshEntity(new ExperienceOrb(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), j));
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, @Nonnull Inventory playerInventory, @Nonnull Player playerEntity) {
        return new GasFiredFurnaceContainer(i, level, worldPosition, playerInventory, playerEntity);
    }

    public int progressFuelCounterPX(int px) {
        if (fuelCounter == 0){
            return 0;
        } else {
            return (px*(((fuelCounter*100)/fuelLength)))/100;
        }
    }

    @Deprecated // Use method that doesn't take in an int instead
    public FluidStack getFluidStackFromTank(int num){
        if (num == 0) {
            return fuelTank.getTank().getFluid();
        }
        return FluidStack.EMPTY;
    }

    public FluidStack getFluidFromTank(){
        return fuelTank.getTank().getFluid();
    }

    public int getTankCapacity(){
        return TANK_CAPACITY;
    }

    @Override
    public @NotNull List<RelationalTank> getRelationalTanks() {
        return fluidManagers;
    }

    public int getFuelCounter(){return fuelCounter;}

    public int getCounter(){return counter;}


    public int progressFuelCounterPercent(){
        if (length != 0){
            return (int)(100-(((float)fuelCounter/(float)fuelLength)*100));
        } else {
            return 0;
        }
    }

    public int progressCounterPercent(){
        if (length != 0){
            return (int)(100-(((float)counter/(float)length)*100));
        } else {
            return 0;
        }
    }

    public boolean countChecker(SmeltingRecipe furnaceRecipe, BlastingRecipe blastingRecipe, ItemStack itemStack){
        if(furnaceRecipe != null){
            return (itemStack.getCount() + furnaceRecipe.getResultItem(level.registryAccess()).getCount()) <= 64;
        } else if (blastingRecipe != null){
            return (itemStack.getCount() + blastingRecipe.getResultItem(level.registryAccess()).getCount()) <= 64;
        }
        return false;
    }

    public boolean itemChecker(SmeltingRecipe furnaceRecipe, BlastingRecipe blastingRecipe, ItemStack itemStack){
        if(furnaceRecipe != null){
            if (itemStack.getItem() == Items.AIR || itemStack.isEmpty()) return true;
            return furnaceRecipe.getResultItem(level.registryAccess()).getItem() == itemStack.getItem();
        } else if (blastingRecipe != null){
            if (itemStack.getItem() == Items.AIR || itemStack.isEmpty()) return true;
            return blastingRecipe.getResultItem(level.registryAccess()).getItem() == itemStack.getItem();
        }
        return false;
    }

    public RelationalTank getFuelTank() {
        return fuelTank;
    }
}
