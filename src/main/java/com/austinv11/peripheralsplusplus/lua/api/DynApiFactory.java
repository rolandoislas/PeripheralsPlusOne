package com.austinv11.peripheralsplusplus.lua.api;

import com.austinv11.peripheralsplusplus.utils.proxy.LuaAPIFactory;
import dan200.computercraft.api.lua.IComputerSystem;
import dan200.computercraft.api.lua.ILuaAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Proxy;

public class DynApiFactory extends LuaAPIFactory {
    @Nullable
    public ILuaAPI create(@Nonnull IComputerSystem computer) {
        try {
            return (ILuaAPI) Proxy.newProxyInstance(
                    Class.forName("dan200.computercraft.api.lua.ILuaAPI").getClassLoader(),
                    new Class[]{Class.forName("dan200.computercraft.api.lua.ILuaAPI")}, new DynApi());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
