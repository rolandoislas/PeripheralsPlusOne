package com.austinv11.peripheralsplusplus.utils;

import com.austinv11.collectiveframework.minecraft.reference.ModIds;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.ManagedPeripheral;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.common.Optional;

@Optional.InterfaceList({
        @Optional.Interface(iface = "li.cil.oc.api.network.ManagedPeripheral", modid = ModIds.OPEN_COMPUTERS_CORE),
        @Optional.Interface(iface = "li.cil.oc.api.network.Environment", modid = ModIds.OPEN_COMPUTERS_CORE)
})
public interface OpenComputersPeripheral extends Environment, ManagedPeripheral, ITickable {
    @Override
    @Optional.Method(modid = ModIds.OPEN_COMPUTERS_CORE)
    default void onConnect(Node node) {

    }

    @Override
    @Optional.Method(modid = ModIds.OPEN_COMPUTERS_CORE)
    default void onDisconnect(Node node) {

    }

    @Override
    @Optional.Method(modid = ModIds.OPEN_COMPUTERS_CORE)
    default void onMessage(Message message) {

    }
}
