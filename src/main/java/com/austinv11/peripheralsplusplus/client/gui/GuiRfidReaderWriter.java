package com.austinv11.peripheralsplusplus.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class GuiRfidReaderWriter extends GuiAnalyzer {
    public GuiRfidReaderWriter(EntityPlayer player, World world, int x, int y, int z) {
        super(player, world, x, y, z);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
    }
}
