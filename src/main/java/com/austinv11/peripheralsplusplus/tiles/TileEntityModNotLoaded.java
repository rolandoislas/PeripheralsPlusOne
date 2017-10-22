package com.austinv11.peripheralsplusplus.tiles;

import com.austinv11.peripheralsplusplus.utils.IPlusPlusPeripheral;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityModNotLoaded extends TileEntity implements IPlusPlusPeripheral {
    public static String publicName = "modNotLoaded";
    private String modId;

    public TileEntityModNotLoaded() {
        super();
    }

    public TileEntityModNotLoaded(String modId) {
        this.modId = modId;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        modId = compound.getString("modId");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setString("modId", modId);
        return super.writeToNBT(compound);
    }

    @Nonnull
    @Override
    public String getType() {
        return String.format("%s_%s", publicName, modId);
    }

    @Nonnull
    @Override
    public String[] getMethodNames() {
        return new String[]{"reason", "error", "message", "missing", "mod", "modid"};
    }

    @Nullable
    @Override
    public Object[] callMethod(@Nonnull IComputerAccess computer, @Nonnull ILuaContext context, int method,
                               @Nonnull Object[] arguments) throws LuaException, InterruptedException {
        return new Object[]{String.format("Mod with mod id \"%s\" is not installed.", modId)};
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return this == other;
    }
}
