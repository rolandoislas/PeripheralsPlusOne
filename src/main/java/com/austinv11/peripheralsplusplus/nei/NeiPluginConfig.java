package com.austinv11.peripheralsplusplus.nei;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import com.austinv11.peripheralsplusplus.init.ModPeripherals;
import com.austinv11.peripheralsplusplus.reference.Reference;
import com.austinv11.peripheralsplusplus.utils.TurtleUtil;
import dan200.computercraft.api.turtle.ITurtleUpgrade;

public class NeiPluginConfig implements IConfigureNEI {
	public NeiPluginConfig() {
		super();
	}

	@Override
	public void loadConfig() {
        for (ITurtleUpgrade upgrade : ModPeripherals.TURTLE_UPGRADES) {
            API.addItemListEntry(TurtleUtil.getTurtle(false, upgrade));
            API.addItemListEntry(TurtleUtil.getTurtle(true, upgrade));
        }
	}

	@Override
	public String getName() {
		return Reference.MOD_NAME+" NEI Plugin";
	}

	@Override
	public String getVersion() {
		return "2.0";
	}
}
