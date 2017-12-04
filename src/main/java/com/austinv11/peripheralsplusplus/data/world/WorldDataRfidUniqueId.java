package com.austinv11.peripheralsplusplus.data.world;

import com.austinv11.peripheralsplusplus.reference.Reference;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nullable;

public class WorldDataRfidUniqueId extends WorldSavedData {
    private static final String TAG_KEY = Reference.MOD_ID + "_rfid_unique_id";
    private long lastId;

    private WorldDataRfidUniqueId() {
        super(TAG_KEY);
    }

    @SuppressWarnings("unused")
    public WorldDataRfidUniqueId(String name) {
        super(name);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        lastId = compound.getLong("last_id");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setLong("last_id", lastId);
        return compound;
    }

    @Nullable
    public static WorldDataRfidUniqueId get() {
        MapStorage storage = DimensionManager.getWorld(0).getMapStorage();
        if (storage == null)
            return null;
        WorldDataRfidUniqueId instance = (WorldDataRfidUniqueId) storage.getOrLoadData(WorldDataRfidUniqueId.class,
                TAG_KEY);
        if (instance == null) {
            instance = new WorldDataRfidUniqueId();
            storage.setData(TAG_KEY, instance);
        }
        return instance;
    }

    public long getLastId() {
        return lastId;
    }

    public void setLastId(long lastId) {
        this.lastId = lastId;
    }
}
