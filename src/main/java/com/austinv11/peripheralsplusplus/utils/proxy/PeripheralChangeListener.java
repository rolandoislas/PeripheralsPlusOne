package com.austinv11.peripheralsplusplus.utils.proxy;

import dan200.computercraft.api.peripheral.IPeripheral;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public abstract class PeripheralChangeListener implements InvocationHandler {

    /**
     * Called when a peripheral updates on the specified side
     * @param side side of the changed peripheral
     * @param newPeripheral new peripheral on the side
     */
    public abstract void onPeripheralChanged(int side, IPeripheral newPeripheral);

    /**
     * Initialize all the peripherals. Called before the default listener is replaced with this one.
     * Return false to signal that the listener is not in a state that is replaceable. It is a good idea to check if
     * peripherals are attached and if not return false.
     */
    public abstract boolean initializePeripherals();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        switch (method.getName()) {
            case "onPeripheralChanged":
                onPeripheralChanged((int) args[0], (IPeripheral) args[1]);
        }
        return null;
    }
}
