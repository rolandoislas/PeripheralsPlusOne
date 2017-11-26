package com.austinv11.peripheralsplusplus.event.handler;

import com.austinv11.peripheralsplusplus.capabilities.nano.CapabilityNanoBot;
import com.austinv11.peripheralsplusplus.capabilities.nano.NanoBotHolder;
import com.austinv11.peripheralsplusplus.capabilities.rfid.CapabilityRfid;
import com.austinv11.peripheralsplusplus.reference.Config;
import com.austinv11.peripheralsplusplus.tiles.TileEntityAntenna;
import net.minecraft.entity.Entity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CapabilitiesHandler {
	@SubscribeEvent
	public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		// Add nano bots
		if (Config.enableNanoBots && !event.getObject().hasCapability(CapabilityNanoBot.INSTANCE, null)) {
			    event.addCapability(CapabilityNanoBot.ID, new CapabilityNanoBot());
			    event.getCapabilities().get(CapabilityNanoBot.ID).getCapability(CapabilityNanoBot.INSTANCE, null)
						.setEntity(event.getObject());
		}
		// Add RFID Tag
		if (Config.enableRfidItems && !event.getObject().hasCapability(CapabilityRfid.INSTANCE, null))
				event.addCapability(CapabilityRfid.ID, new CapabilityRfid());
	}

	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event) {
		// Register entity with the antenna registry
		NanoBotHolder nanoBotHolder = event.getEntity().getCapability(CapabilityNanoBot.INSTANCE, null);
		if (nanoBotHolder != null) {
			if (nanoBotHolder.getAntenna() != null &&
					TileEntityAntenna.ANTENNA_REGISTRY.containsKey(nanoBotHolder.getAntenna())) {
				TileEntityAntenna tileEntityAntenna = TileEntityAntenna.ANTENNA_REGISTRY.get(
						nanoBotHolder.getAntenna());
				tileEntityAntenna.registerEntity(event.getEntity());
			}
		}
	}
}
