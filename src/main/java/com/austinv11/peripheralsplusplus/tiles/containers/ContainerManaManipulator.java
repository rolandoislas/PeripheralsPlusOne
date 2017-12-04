package com.austinv11.peripheralsplusplus.tiles.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import vazkii.botania.api.mana.ILens;

public class ContainerManaManipulator extends ContainerChest {
    public ContainerManaManipulator(IInventory inventory, IInventory tileEntity, EntityPlayer player) {
        super(inventory, tileEntity, player);
        // Add lens slots
        for (int slotIndex = 0; slotIndex < tileEntity.getSizeInventory(); slotIndex++) {
            Slot slot = getSlot(slotIndex);
            inventorySlots.set(slotIndex, new LensSlot(slot.inventory, slot.getSlotIndex(), slot.xDisplayPosition,
                    slot.yDisplayPosition, slot.slotNumber));
        }
    }

    private class LensSlot extends Slot {
        private LensSlot(IInventory inventoryIn, int index, int xPosition, int yPosition, int slotNumber) {
            super(inventoryIn, index, xPosition, yPosition);
            this.slotNumber = slotNumber;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return stack.getItem() instanceof ILens;
        }
    }
}
