package com.austinv11.peripheralsplusplus.tiles.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

public class ContainerRfidReaderWriter extends ContainerAnalyzer {
    public ContainerRfidReaderWriter(EntityPlayer player, IInventory inv, int xSize, int ySize) {
        super(player, inv, xSize, ySize);
    }
}
