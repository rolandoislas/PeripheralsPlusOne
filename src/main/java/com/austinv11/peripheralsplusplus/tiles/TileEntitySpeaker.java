package com.austinv11.peripheralsplusplus.tiles;

import com.austinv11.peripheralsplusplus.PeripheralsPlusPlus;
import com.austinv11.peripheralsplusplus.network.SynthPacket;
import com.austinv11.peripheralsplusplus.reference.Config;
import com.austinv11.peripheralsplusplus.utils.IPlusPlusPeripheral;
import com.voicerss.tts.Languages;
import com.voicerss.tts.VoiceParameters;
import com.voicerss.tts.VoiceProvider;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.TurtleSide;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.*;

public class TileEntitySpeaker extends TileEntity implements IPlusPlusPeripheral {
	private ITurtleAccess turtle;
	private TurtleSide side = null;
	private int id;
	private List<IComputerAccess> computers = new ArrayList<>();
	private Map<UUID, Long> pendingEvents = new HashMap<>();

	public TileEntitySpeaker() {
		super();
	}

	public TileEntitySpeaker(ITurtleAccess turtle, TurtleSide side) {
		this();
		this.turtle = turtle;
		this.side = side;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		return nbttagcompound;
	}

	public void update() {
		if (turtle != null) {
			this.setWorld(turtle.getWorld());
			this.setPos(turtle.getPosition());
		}
		if (world != null)
			id = world.provider.getDimension();
		synchronized (this) {
			for (Map.Entry<UUID, Long> pendingEvent : pendingEvents.entrySet())
				if (System.currentTimeMillis() - pendingEvent.getValue() > 30000) {
					onSpeechCompletion("", pendingEvent.getKey(), true, "");
					break;
				}
		}
	}

	@Override
	public String getType() {
		return "speaker";
	}

	@Override
	public String[] getMethodNames() {
		VoiceProvider voiceProvider = new VoiceProvider("");
		VoiceParameters voiceParameters = new VoiceParameters("bonjour", Languages.French_France);
		return new String[]{
				"speak", "synthesize", /*text, [range, [voice, [pitch, [pitchRange, [pitchShift, [rate, [volume, [wait]]]]]]]]*/
				"web" /*text, [range, [language, [rate, [volume, [wait, [apiKey]]]]]]*/
		};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments)
			throws LuaException, InterruptedException {
		if (!Config.enableSpeaker)
			throw new LuaException("Speakers have been disabled");
		if (method <= 2) {
			// speak/synthesize
			if (method <= 1) {
				if (!(arguments.length > 0) || !(arguments[0] instanceof String))
					throw new LuaException("Bad argument #1 (expected string)");
				if (arguments.length > 1 && !(arguments[1] instanceof Double))
					throw new LuaException("Bad argument #2 (expected number)");
				if (arguments.length > 2 && !(arguments[2] instanceof String))
					throw new LuaException("Bad argument #3 (expected string)");
				if (arguments.length > 3 && !(arguments[3] instanceof Double))
					throw new LuaException("Bad argument #4 (expected number)");
				if (arguments.length > 4 && !(arguments[4] instanceof Double))
					throw new LuaException("Bad argument #5 (expected number)");
				if (arguments.length > 5 && !(arguments[5] instanceof Double))
					throw new LuaException("Bad argument #6 (expected number)");
				if (arguments.length > 6 && !(arguments[6] instanceof Double))
					throw new LuaException("Bad argument #7 (expected number)");
				if (arguments.length > 7 && !(arguments[7] instanceof Double))
					throw new LuaException("Bad argument #8 (expected number)");
				if (arguments.length > 8 && !(arguments[8] instanceof Boolean))
					throw new LuaException("Bad argument #9 (expected boolean");
			}
			// web
			else {
				if (!(arguments.length > 0) || !(arguments[0] instanceof String))
					throw new LuaException("Bad argument #1 (expected string)");
				if (arguments.length > 1 && !(arguments[1] instanceof Double))
					throw new LuaException("Bad argument #2 (expected number)");
				if (arguments.length > 2 && !(arguments[2] instanceof String))
					throw new LuaException("Bad argument #3 (expected string)");
				if (arguments.length > 3 && !(arguments[3] instanceof Double))
					throw new LuaException("Bad argument #4 (expected number)");
				if (arguments.length > 4 && !(arguments[4] instanceof Double))
					throw new LuaException("Bad argument #5 (expected number)");
				if (arguments.length > 5 && !(arguments[5] instanceof Boolean))
					throw new LuaException("Bad argument #6 (expected boolean");
				if (arguments.length > 6 && !(arguments[6] instanceof String))
					throw new LuaException("Bad argument #7 (expected string");
			}
			
			String text = (String) arguments[0];
			double range;
			if (Config.speechRange < 0)
				range = Double.MAX_VALUE;
			else
				range = Config.speechRange;
			if (arguments.length > 1)
				range = (Double) arguments[1];
			String voice = arguments.length > 2 ? (String) arguments[2] :
					(method == 2 ? Languages.English_UnitedStates : "kevin16");
			Float pitch = arguments.length > 3 && method <= 1 ? ((Double)arguments[3]).floatValue() : null;
			Float pitchRange = arguments.length > 4 && method <= 1 ? ((Double)arguments[4]).floatValue() : null;
			Float pitchShift = arguments.length > 5 && method <= 1 ? ((Double)arguments[5]).floatValue() : null;
			Float rate = arguments.length > 6 && method <= 1 ? ((Double)arguments[6]).floatValue() : null;
			if (arguments.length > 3 && method == 2)
				rate = ((Double)arguments[3]).floatValue();
			Float volume = arguments.length > 7 ? ((Double)arguments[7]).floatValue() : null;
			if (arguments.length > 4 && method == 2)
				volume = ((Double)arguments[4]).floatValue();
			boolean waitForCompletion = (arguments.length > 8 && method <= 1 && (Boolean) arguments[8]) ||
					(arguments.length > 5 && method == 2 && (Boolean) arguments[5]);
			String apiKey = arguments.length > 6 && method == 2 ? (String) arguments[6] : null;
			if (apiKey == null || apiKey.isEmpty())
				apiKey = Config.voiceRssApiKey;

			UUID eventId = null;
			while (eventId == null || pendingEvents.containsKey(eventId))
				eventId = UUID.randomUUID();
			pendingEvents.put(eventId, System.currentTimeMillis());
			PeripheralsPlusPlus.NETWORK.sendToAllAround(
			        new SynthPacket(text, voice, pitch, pitchRange, pitchShift, rate, volume, getPos(), id, side,
							eventId, method == 2, apiKey),
                    new NetworkRegistry.TargetPoint(id, getPos().getX(), getPos().getY(), getPos().getZ(), range));
			
			if (waitForCompletion) {
				Object[] synthEvent;
				do {
					synthEvent = context.pullEvent("synthComplete");
				}
				while (synthEvent.length < 3 || !eventId.toString().equals(synthEvent[2]));
			}
			return new Object[]{eventId.toString()};
		}
		return new Object[0];
	}

	@Override
	public void attach(IComputerAccess computer) {
		computers.add(computer);
	}

	@Override
	public void detach(IComputerAccess computer) {
		computers.remove(computer);
	}

	@Override
	public boolean equals(IPeripheral other) {
		return (this == other);
	}

	public void onSpeechCompletion(String text, UUID eventId, boolean success, String errorMessage) {
		synchronized (this) {
			if (!pendingEvents.containsKey(eventId))
				return;
			pendingEvents.remove(eventId);
		}
		for (IComputerAccess computer : computers)
			computer.queueEvent("synthComplete", new Object[]{text, eventId.toString(), success, errorMessage});
	}
}
