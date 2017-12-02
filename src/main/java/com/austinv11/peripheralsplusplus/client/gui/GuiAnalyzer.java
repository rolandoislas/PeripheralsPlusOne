package com.austinv11.peripheralsplusplus.client.gui;

import com.austinv11.peripheralsplusplus.reference.Reference;
import com.austinv11.peripheralsplusplus.tiles.containers.ContainerAnalyzer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class GuiAnalyzer extends GuiContainer {

	private ITextComponent name;
	private int x, y, z;
	private EntityPlayer player;
	private World world;
	private int sizeX, sizeY;
	private ResourceLocation backgroundimage = new ResourceLocation(Reference.MOD_ID.toLowerCase() + ":" +
			"textures/gui/analyzer.png");

	public GuiAnalyzer(EntityPlayer player, World world, int x, int y, int z) {
		super(new ContainerAnalyzer(player, (IInventory)world.getTileEntity(new BlockPos(x, y, z)), 176, 166));
		this.x = x;
		this.y = y;
		this.z = z;
		this.player = player;
		this.world = world;
		sizeX = 176;
		sizeY = 166;
		TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
		name = new TextComponentString("null");
		if (tileEntity != null)
			name = tileEntity.getDisplayName();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(backgroundimage);
		int x = (width - sizeX) / 2;
		int y = (height - sizeY) / 2;
		drawTexturedModalRect(x, y, 0, 0, sizeX, sizeY);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.fontRendererObj.drawString(name.getUnformattedText(), 8, 6, 4210752);
		this.fontRendererObj.drawString(player.inventory.getDisplayName().getUnformattedText(), 8, this.ySize - 98, 4210752);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		Slot hoveredSlot = getSlotUnderMouse();
		if (mc.thePlayer.inventory.getItemStack() == null && hoveredSlot != null && hoveredSlot.getHasStack())
			renderToolTip(hoveredSlot.getStack(), mouseX, mouseY);
	}
}
