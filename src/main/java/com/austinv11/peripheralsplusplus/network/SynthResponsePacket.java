package com.austinv11.peripheralsplusplus.network;

import com.austinv11.peripheralsplusplus.tiles.TileEntitySpeaker;
import com.austinv11.peripheralsplusplus.utils.ReflectionHelper;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.TurtleSide;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class SynthResponsePacket implements IMessage {

	private UUID eventId;
	private BlockPos pos;
    public String text;
	public int x,y,z;
	public World world;
	public TurtleSide side;
	
	public SynthResponsePacket(){}
	
	public SynthResponsePacket(String text, BlockPos pos, World world, TurtleSide side, UUID eventId) {
		this.text = text;
		this.pos = pos;
		this.world = world;
		this.side = side;
		this.eventId = eventId;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		NBTTagCompound tag = ByteBufUtils.readTag(buf);
		text = tag.getString("text");
        int[] posArray = tag.getIntArray("pos");
        pos = new BlockPos(posArray[0], posArray[1], posArray[2]);
		world = DimensionManager.getWorld(tag.getInteger("dim"));
		side = tag.getString("side").equals("null") ? null : TurtleSide.valueOf(tag.getString("side"));
		eventId = tag.getUniqueId("eventId");
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("text", text);
        tag.setIntArray("pos", new int[]{pos.getX(), pos.getY(), pos.getZ()});
		tag.setInteger("dim", world.provider.getDimension());
		tag.setString("side", side == null ? "null" : side.name());
		tag.setUniqueId("eventId", eventId);
		ByteBufUtils.writeTag(buf, tag);
	}
	
	public static class SynthResponsePacketHandler implements IMessageHandler<SynthResponsePacket, IMessage> {
		
		@Override
		public IMessage onMessage(SynthResponsePacket message, MessageContext ctx) {
			if (message.side == null) {
				TileEntity tileEntity = message.world.getTileEntity(message.pos);
				if (tileEntity != null)
					((TileEntitySpeaker)tileEntity).onSpeechCompletion(message.text, message.eventId);
			}
			else
				try {
					ITurtleAccess turtle = ReflectionHelper.getTurtle(message.world.getTileEntity(message.pos));
					if (turtle != null) {
						IPeripheral tileEntity = turtle.getPeripheral(message.side);
						if (tileEntity != null)
							((TileEntitySpeaker) tileEntity).onSpeechCompletion(message.text, message.eventId);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			return null;
		}
	}
}
