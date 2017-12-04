package com.austinv11.peripheralsplusplus.capabilities.rfid;

import com.austinv11.peripheralsplusplus.reference.Reference;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityRfid implements ICapabilitySerializable<NBTBase> {
    @CapabilityInject(RfidTagHolder.class)
    public static final Capability<RfidTagHolder> INSTANCE = null;
    public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID,
            "rfid_tagged_entity");
    private RfidTagHolder instance = INSTANCE.getDefaultInstance();

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability.equals(INSTANCE);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return hasCapability(capability, facing) ? INSTANCE.cast(instance) : null;
    }

    @Override
    public NBTBase serializeNBT() {
        return INSTANCE.getStorage().writeNBT(INSTANCE, instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        INSTANCE.getStorage().readNBT(INSTANCE, instance, null, nbt);
    }
}
