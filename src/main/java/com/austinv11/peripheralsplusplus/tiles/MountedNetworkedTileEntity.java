package com.austinv11.peripheralsplusplus.tiles;

import com.austinv11.collectiveframework.minecraft.tiles.NetworkedTileEntity;
import com.austinv11.peripheralsplusplus.mount.DynamicMount;
import com.austinv11.peripheralsplusplus.utils.IPlusPlusPeripheral;
import dan200.computercraft.api.peripheral.IComputerAccess;

import java.util.ArrayList;
import java.util.List;

public abstract class MountedNetworkedTileEntity extends NetworkedTileEntity implements IPlusPlusPeripheral {
	private List<String> mounts;

	public MountedNetworkedTileEntity() {
		super();
		mounts = new ArrayList<>();
	}

	@Override
	public void attach(IComputerAccess computer) {
		mounts.addAll(DynamicMount.attach(computer, this));
	}

	@Override
	public void detach(IComputerAccess computer) {
		DynamicMount.detach(computer, mounts);
	}
}
