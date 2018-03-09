package com.austinv11.peripheralsplusplus.capabilities.rfid;

import net.minecraft.item.ItemStack;

public class RfidTagHolderDefault implements RfidTagHolder {
    private boolean prodded;
    private ItemStack tag = ItemStack.EMPTY;

    @Override
    public void setProdded(boolean prodded) {
        this.prodded = prodded;
    }

    @Override
    public boolean hasBeenProdded() {
        return prodded;
    }

    @Override
    public ItemStack getTag() {
        return tag;
    }

    @Override
    public void setTag(ItemStack tag) {
        this.tag = tag;
    }
}
