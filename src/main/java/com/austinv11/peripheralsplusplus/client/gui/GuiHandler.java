package com.austinv11.peripheralsplusplus.client.gui;

import com.austinv11.peripheralsplusplus.reference.Reference;
import com.austinv11.peripheralsplusplus.tiles.containers.ContainerAnalyzer;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		if (id == Reference.GUIs.ANALYZER.ordinal()) {
			return new ContainerAnalyzer(player, (IInventory) world.getTileEntity(x, y, z), 176, 166);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		if (id == Reference.GUIs.ANALYZER.ordinal()) {
			return new GuiAnalyzer(player, world, x, y, z);
		}
		return null;
	}
}