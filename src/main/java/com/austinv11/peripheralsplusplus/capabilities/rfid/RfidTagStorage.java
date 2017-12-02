package com.austinv11.peripheralsplusplus.capabilities.rfid;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class RfidTagStorage implements Capability.IStorage<RfidTagHolder> {
    @Nullable
    @Override
    public NBTBase writeNBT(Capability<RfidTagHolder> capability, RfidTagHolder instance, EnumFacing side) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("prodded", instance.hasBeenProdded());
        tag.setTag("rfid_tag", instance.getTag() == null ? new NBTTagCompound() :
                instance.getTag().writeToNBT(new NBTTagCompound()));
        return tag;
    }

    @Override
    public void readNBT(Capability<RfidTagHolder> capability, RfidTagHolder instance, EnumFacing side, NBTBase nbt) {
        NBTTagCompound tag = (NBTTagCompound)nbt;
        instance.setProdded(tag.getBoolean("prodded"));
        instance.setTag(ItemStack.loadItemStackFromNBT(tag.getCompoundTag("rfid_tag")));
    }
}
