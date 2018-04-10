package com.austinv11.peripheralsplusplus.utils.proxy;

import dan200.computercraft.api.lua.IComputerSystem;
import dan200.computercraft.api.lua.ILuaAPI;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public abstract class LuaAPIFactory implements InvocationHandler {
    @Nullable
    public abstract ILuaAPI create(IComputerSystem computerSystem);

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        switch (method.getName()) {
            case "create":
                return create((IComputerSystem) args[0]);
            case "hashCode":
                return this.hashCode();
        }
        return null;
    }
}
