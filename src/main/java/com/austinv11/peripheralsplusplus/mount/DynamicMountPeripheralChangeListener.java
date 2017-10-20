package com.austinv11.peripheralsplusplus.mount;

import com.austinv11.peripheralsplusplus.utils.ReflectionHelper;
import com.austinv11.peripheralsplusplus.utils.proxy.PeripheralChangeListener;
import com.austinv11.peripheralsplusplus.utils.proxy.Task;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

import java.lang.reflect.InvocationTargetException;

public class DynamicMountPeripheralChangeListener extends PeripheralChangeListener {

    private final Object peripheralChangeListener;
    private final Object computer;

    /**
     * Construct a new peripheral change lister that adds the dynamic mount and makes calls to the original listener
     * @param peripheralChangeListener Original IPeripheralChangeListener
     */
    public DynamicMountPeripheralChangeListener(Object computer, Object peripheralChangeListener) {
        this.computer = computer;
        this.peripheralChangeListener = peripheralChangeListener;
    }

    @Override
    public void onPeripheralChanged(int side, IPeripheral newPeripheral) {
        try {
            ReflectionHelper.onPeripheralChanged(side, newPeripheral, peripheralChangeListener);
            Object wrappedPeripheral = ReflectionHelper.getPeripheralWrapperFromChangeListener(peripheralChangeListener,
                    side);
            if (newPeripheral != null && wrappedPeripheral != null)
                ReflectionHelper.queueTask(new Task() {
                    @Override
                    public Object getOwner() {
                        return computer;
                    }

                    @Override
                    public void execute() {
                        DynamicMount.attach((IComputerAccess) wrappedPeripheral, newPeripheral);
                    }
                }, null);
        }
        catch (NoSuchMethodException | ClassNotFoundException | InvocationTargetException | IllegalAccessException |
                NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean initializePeripherals() {
        for (int side = 0; side < 6; side++) {
            try {
                Object wrappedPeripheral = ReflectionHelper
                        .getPeripheralWrapperFromChangeListener(peripheralChangeListener, side);
                if (wrappedPeripheral == null)
                    continue;
                if (!ReflectionHelper.isWrappedPeripheralAttached(wrappedPeripheral))
                    return false;
                IPeripheral peripheral = ReflectionHelper.getPeripheral(computer, side);
                if (peripheral == null)
                    continue;
                onPeripheralChanged(side, peripheral);
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                    IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
