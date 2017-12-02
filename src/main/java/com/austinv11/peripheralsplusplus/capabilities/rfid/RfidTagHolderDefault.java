package com.austinv11.peripheralsplusplus.capabilities.rfid;

import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public class RfidTagHolderDefault implements RfidTagHolder {
    private boolean prodded;
    private ItemStack tag = null;

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
    public void setTag(@Nullable ItemStack tag) {
        this.tag = tag;
    }
}
