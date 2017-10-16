package com.austinv11.peripheralsplusplus.entities;

import com.austinv11.peripheralsplusplus.mount.DynamicMount;
import com.austinv11.peripheralsplusplus.utils.IPlusPlusPeripheral;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class MountedEntity extends Entity implements IPlusPlusPeripheral {
    private List<String> mounts;

    public MountedEntity(World worldIn) {
        super(worldIn);
        mounts = new ArrayList<>();
    }

    @Override
    public void attach(@Nonnull IComputerAccess computer) {
        mounts.addAll(DynamicMount.attach(computer, this));
    }

    @Override
    public void detach(@Nonnull IComputerAccess computer) {
        DynamicMount.detach(computer, mounts);
    }
}
