package com.veteam.voluminousenergy.blocks.containers;

import com.veteam.voluminousenergy.VoluminousEnergy;
import com.veteam.voluminousenergy.blocks.blocks.VEBlocks;
import com.veteam.voluminousenergy.blocks.inventory.slots.VEBucketSlot;
import com.veteam.voluminousenergy.blocks.inventory.slots.VEInsertSlot;
import com.veteam.voluminousenergy.blocks.tiles.IVEPoweredTileEntity;
import com.veteam.voluminousenergy.blocks.tiles.VETileEntity;
import com.veteam.voluminousenergy.tools.sidemanager.VESlotManager;
import com.veteam.voluminousenergy.util.SlotType;
import com.veteam.voluminousenergy.util.TagUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import java.util.List;

import static com.veteam.voluminousenergy.blocks.blocks.VEBlocks.CENTRIFUGAL_AGITATOR_CONTAINER;

public class CentrifugalAgitatorContainer extends VoluminousContainer {

    private static final int NUMBER_OF_SLOTS = 7;

    public CentrifugalAgitatorContainer(int id, Level world, BlockPos pos, Inventory inventory, Player player) {
        super(CENTRIFUGAL_AGITATOR_CONTAINER.get(), id);
        this.tileEntity = (VETileEntity) world.getBlockEntity(pos);
        this.tileEntity.getCapability(ForgeCapabilities.ITEM_HANDLER);
        this.playerEntity = player;
        this.playerInventory = new InvWrapper(inventory);

        tileEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(h -> {
            addSlot(new VEBucketSlot(h, 0, 38, 18)); // Bucket insert; input tank
            addSlot(new VEBucketSlot(h, 1, 38, 49)); // Bucket extract; input tank
            addSlot(new VEBucketSlot(h, 2, 96, 18)); // Bucket insert; first output tank
            addSlot(new VEBucketSlot(h, 3, 96, 49)); // Bucket extract; first output tank
            addSlot(new VEBucketSlot(h, 4, 137, 18)); // Bucket insert; second output tank
            addSlot(new VEBucketSlot(h, 5, 137, 49)); // Bucket extract; second output tank
            addSlot(new VEInsertSlot(h, 6, 130, -14)); // Upgrade slot
        });
        layoutPlayerInventorySlots(8, 84);

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return getEnergy();
            }

            @Override
            public void set(int value) {
            }
        });
    }

    public int getEnergy() {
        return tileEntity.getCapability(ForgeCapabilities.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
    }

    public int powerScreen(int px) {
        int stored = tileEntity.getCapability(ForgeCapabilities.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
        int max = tileEntity.getCapability(ForgeCapabilities.ENERGY).map(IEnergyStorage::getMaxEnergyStored).orElse(0);
        int ret = (((stored * 100 / max * 100) / 100) * px) / 100;
        return ret;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return stillValid(ContainerLevelAccess.create(tileEntity.getLevel(), tileEntity.getBlockPos()), playerEntity, VEBlocks.CENTRIFUGAL_AGITATOR_BLOCK.get());
    }

    private void layoutPlayerInventorySlots(int leftCol, int topRow) {
        // Player inventory
        addSlotBox(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);

        // Hotbar
        topRow += 58;
        addSlotRange(playerInventory, 0, leftCol, topRow, 9, 18);
    }
}
