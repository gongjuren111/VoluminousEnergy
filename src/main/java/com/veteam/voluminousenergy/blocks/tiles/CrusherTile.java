package com.veteam.voluminousenergy.blocks.tiles;

import com.veteam.voluminousenergy.blocks.blocks.VEBlocks;
import com.veteam.voluminousenergy.blocks.containers.CrusherContainer;
import com.veteam.voluminousenergy.recipe.CrusherRecipe;
import com.veteam.voluminousenergy.tools.Config;
import com.veteam.voluminousenergy.tools.VEEnergyStorage;
import com.veteam.voluminousenergy.tools.VESidedItemManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import static net.minecraft.util.math.MathHelper.abs;

public class CrusherTile extends VoluminousTileEntity implements ITickableTileEntity, INamedContainerProvider {
    //private LazyOptional<IItemHandler> handler = LazyOptional.of(this::createHandler);
    private LazyOptional<IEnergyStorage> energy = LazyOptional.of(this::createEnergy);
    private ServerPlayerEntity player;

    // Side IO status
    public VESidedItemManager inputSlotProp = new VESidedItemManager(0,Direction.UP,true);
    public VESidedItemManager outputSlotProp = new VESidedItemManager(1,Direction.DOWN,true);
    public VESidedItemManager rngSlotProp = new VESidedItemManager(2, Direction.NORTH,true);


    // Sided Item Handlers
    private LazyOptional<ItemStackHandler> handler = LazyOptional.of(() -> this.inventory);
    private LazyOptional<IItemHandlerModifiable> inputItemHandler = LazyOptional.of(() -> new RangedWrapper(this.inventory, 0, 1));
    private LazyOptional<IItemHandlerModifiable> outputItemHandler = LazyOptional.of(() -> new RangedWrapper(this.inventory, 1, 2));
    private LazyOptional<IItemHandlerModifiable> rngItemHandler = LazyOptional.of(() -> new RangedWrapper(this.inventory, 2, 3));

    private int counter;
    private int length;
    private AtomicReference<ItemStack> inputItemStack = new AtomicReference<ItemStack>(new ItemStack(Items.AIR,0));
    private static final Logger LOGGER = LogManager.getLogger();


    public CrusherTile(){
        super(VEBlocks.CRUSHER_TILE);
    }

    public final ItemStackHandler inventory = new ItemStackHandler(3) {
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) { //IS ITEM VALID PLEASE DO THIS PER SLOT TO SAVE DEBUG HOURS!!!!
            ItemStack referenceStack = stack.copy();
            referenceStack.setCount(64);
            CrusherRecipe recipe = world.getRecipeManager().getRecipe(CrusherRecipe.recipeType, new Inventory(referenceStack), world).orElse(null);
            CrusherRecipe recipe1 = world.getRecipeManager().getRecipe(CrusherRecipe.recipeType, new Inventory(inputItemStack.get().copy()),world).orElse(null);

            if (slot == 0 && recipe != null){
                return recipe.ingredient.test(stack);
            } else if (slot == 1 && recipe1 != null){
                return stack.getItem() == recipe1.result.getItem();
            } else if (slot == 2 && recipe1 != null){
                return stack.getItem() == recipe1.getRngItem().getItem();
            }
            return false;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate){ //ALSO DO THIS PER SLOT BASIS TO SAVE DEBUG HOURS!!!
            ItemStack referenceStack = stack.copy();
            referenceStack.setCount(64);
            CrusherRecipe recipe = world.getRecipeManager().getRecipe(CrusherRecipe.recipeType, new Inventory(referenceStack), world).orElse(null);
            CrusherRecipe recipe1 = world.getRecipeManager().getRecipe(CrusherRecipe.recipeType, new Inventory(inputItemStack.get().copy()),world).orElse(null);

            if(slot == 0 && recipe != null) {
                for (ItemStack testStack : recipe.ingredient.getMatchingStacks()){
                    if(stack.getItem() == testStack.getItem()){
                        return super.insertItem(slot, stack, simulate);
                    }
                }
            } else if (slot == 1 && recipe1 != null){
                if (stack.getItem() == recipe1.result.getItem()){
                    return super.insertItem(slot, stack, simulate);
                }
            } else if (slot == 2 && recipe1 != null){
                if (stack.getItem() == recipe1.getRngItem().getItem()){
                    return super.insertItem(slot, stack, simulate);
                }
            }
            return stack;
        }

        @Override
        @Nonnull
        public ItemStack extractItem(int slot, int amount, boolean simulate){
            return super.extractItem(slot,amount,simulate);
        }

        @Override
        protected void onContentsChanged(final int slot) {
            super.onContentsChanged(slot);
            CrusherTile.this.markDirty();
        }
    };

    @Override
    public void tick(){

        updateClients();

        ItemStack input = inventory.getStackInSlot(0).copy();
        ItemStack output = inventory.getStackInSlot(1).copy();
        ItemStack rng = inventory.getStackInSlot(2).copy();

        CrusherRecipe recipe = world.getRecipeManager().getRecipe(CrusherRecipe.recipeType, new Inventory(input), world).orElse(null);
        inputItemStack.set(input.copy()); // Atomic Reference, use this to query recipes

        if (!input.isEmpty()){
            if (output.getCount() + recipe.getOutputAmount() < 64 && rng.getCount() + recipe.getOutputRngAmount() < 64 && this.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0) > 0) {
                if (counter == 1){ //The processing is about to be complete
                    // Extract the inputted item
                    inventory.extractItem(0,recipe.ingredientCount,false);

                    // Get output stack from the recipe
                    ItemStack newOutputStack = recipe.getResult().copy();

                    //LOGGER.debug("output: " + output + " rng: " + rng + " newOutputStack: "  + newOutputStack);

                    // Manipulating the Output slot
                    if (output.getItem() != newOutputStack.getItem() || output.getItem() == Items.AIR) {
                        if(output.getItem() == Items.AIR){ // Fix air >1 jamming slots
                            output.setCount(1);
                        }
                        newOutputStack.setCount(recipe.getOutputAmount());
                        inventory.insertItem(1,newOutputStack.copy(),false); // CRASH the game if this is not empty!
                    } else { // Assuming the recipe output item is already in the output slot
                        output.setCount(recipe.getOutputAmount()); // Simply change the stack to equal the output amount
                        inventory.insertItem(1,output.copy(),false); // Place the new output stack on top of the old one
                    }

                    // Manipulating the RNG slot
                    if (recipe.getChance() != 0){ // If the chance is ZERO, this functionality won't be used
                        ItemStack newRngStack = recipe.getRngItem().copy();

                        // Generate Random floats
                        Random r = new Random();
                        float random = abs(0 + r.nextFloat() * (0 - 1));
                        //LOGGER.debug("Random: " + random);
                        // ONLY manipulate the slot if the random float is under or is identical to the chance float
                        if(random <= recipe.getChance()){
                            //LOGGER.debug("Chance HIT!");
                            if (rng.getItem() != recipe.getRngItem().getItem()){
                                if (rng.getItem() == Items.AIR){
                                    rng.setCount(1);
                                }
                                newRngStack.setCount(recipe.getOutputRngAmount());
                                inventory.insertItem(2, newRngStack.copy(),false); // CRASH the game if this is not empty!
                            } else { // Assuming the recipe output item is already in the output slot
                                rng.setCount(recipe.getOutputRngAmount()); // Simply change the stack to equal the output amount
                                inventory.insertItem(2,rng.copy(),false); // Place the new output stack on top of the old one
                            }
                        }
                    }
                    counter--;
                    energy.ifPresent(e -> ((VEEnergyStorage)e).consumeEnergy(Config.CRUSHER_POWER_USAGE.get()));
                    markDirty();
                } else if (counter > 0){ //In progress
                    counter--;
                    energy.ifPresent(e -> ((VEEnergyStorage)e).consumeEnergy(Config.CRUSHER_POWER_USAGE.get()));
                } else { // Check if we should start processing
                    if (output.isEmpty() && rng.isEmpty() || output.isEmpty() && rng.getItem() == recipe.getRngItem().getItem() || output.getItem() == recipe.getResult().getItem() && rng.getItem() == recipe.getRngItem().getItem() || output.getItem() == recipe.getResult().getItem() && rng.isEmpty()){
                        counter = recipe.getProcessTime();
                        length = counter;
                    } else {
                        counter = 0;
                    }
                }
            } else { // This is if we reach the maximum in the slots
                counter = 0;
            }
        } else { // this is if the input slot is empty
            counter = 0;
        }
    }

    /*
        Read and Write on World save
     */

    @Override
    public void read(CompoundNBT tag){
        CompoundNBT inv = tag.getCompound("inv");
        this.inventory.deserializeNBT(inv);
        //createHandler().deserializeNBT(inv);
        CompoundNBT energyTag = tag.getCompound("energy");
        energy.ifPresent(h -> ((INBTSerializable<CompoundNBT>)h).deserializeNBT(energyTag));
        super.read(tag);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        tag.put("inv", this.inventory.serializeNBT());
        energy.ifPresent(h -> {
            CompoundNBT compound = ((INBTSerializable<CompoundNBT>)h).serializeNBT();
            tag.put("energy",compound);
        });
        return super.write(tag);
    }

    private IEnergyStorage createEnergy(){
        return new VEEnergyStorage(Config.CRUSHER_MAX_POWER.get(),Config.CRUSHER_TRANSFER.get()); // Max Power Storage, Max transfer
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SUpdateTileEntityPacket pkt){
        energy.ifPresent(e -> ((VEEnergyStorage)e).setEnergy(pkt.getNbtCompound().getInt("energy")));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (side == null) {
                return handler.cast();
            } else {
                // 1 = top, 0 = bottom, 2 = north, 3 = south, 4 = west, 5 = east
                if (side.getIndex() == inputSlotProp.side.getIndex() && inputSlotProp.enabled){
                    return inputItemHandler.cast();
                }
                if (side.getIndex() == outputSlotProp.side.getIndex() && outputSlotProp.enabled){
                    return outputItemHandler.cast();
                }
                if (side.getIndex() == rngSlotProp.side.getIndex() && rngSlotProp.enabled){
                    return rngItemHandler.cast();
                }
            }
        }
        if (cap == CapabilityEnergy.ENERGY){
            return energy.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public ITextComponent getDisplayName(){
        return new StringTextComponent(getType().getRegistryName().getPath());
    }

    @Nullable
    @Override
    public Container createMenu(int i, @Nonnull PlayerInventory playerInventory, @Nonnull PlayerEntity playerEntity)
    {
        return new CrusherContainer(i,world,pos,playerInventory,playerEntity);
    }

    public int progressCounterPX(int px){
        if (counter == 0){
            return 0;
        } else {
            return (px*(100-((counter*100)/length)))/100;
        }
    }

}
