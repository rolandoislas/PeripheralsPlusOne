package com.austinv11.peripheralsplusplus.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.items.IItemHandler;

public class ItemHandlerInventoryWrapper implements IInventory {
    private final IItemHandler itemHandler;

    public ItemHandlerInventoryWrapper(IItemHandler itemHandler) {
        this.itemHandler = itemHandler;
    }

    @Override
    public int getSizeInventory() {
        return itemHandler.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int slotIndex = 0; slotIndex < itemHandler.getSlots(); slotIndex++) {
            ItemStack slot = itemHandler.getStackInSlot(slotIndex);
            if (!slot.isEmpty())
                return false;
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return itemHandler.getStackInSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return itemHandler.extractItem(index, count, false);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return itemHandler.extractItem(index, itemHandler.getSlotLimit(index), false);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        itemHandler.extractItem(index, itemHandler.getSlotLimit(index), false);
        itemHandler.insertItem(index, stack, false);
    }

    @Override
    public int getInventoryStackLimit() {
        if (itemHandler.getSlots() > 0)
            return itemHandler.getSlotLimit(0);
        return 0;
    }

    @Override
    public void markDirty() {

    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        ItemStack item = itemHandler.getStackInSlot(index);
        return item.isEmpty() || (ItemStack.areItemsEqual(stack, item) && ItemStack.areItemStackTagsEqual(stack, item));
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {

    }

    @Override
    public String getName() {
        return "IItemHandler";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(getName());
    }
}
