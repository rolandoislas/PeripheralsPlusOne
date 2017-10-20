package com.austinv11.peripheralsplusplus.turtles.peripherals;

import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

import java.util.ArrayList;
import java.util.List;

public abstract class MountedPeripheral implements IPeripheral {
	private List<String> mounts;

	public MountedPeripheral() {
		super();
		mounts = new ArrayList<>();
	}

	@Override
	public void attach(IComputerAccess computer) {
		//mounts.addAll(DynamicMount.attach(computer, this));
	}

	@Override
	public void detach(IComputerAccess computer) {
		//DynamicMount.detach(computer, mounts);
	}
}
