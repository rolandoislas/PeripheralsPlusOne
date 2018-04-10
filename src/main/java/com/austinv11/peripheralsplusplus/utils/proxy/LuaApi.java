package com.austinv11.peripheralsplusplus.utils.proxy;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaObject;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public abstract class LuaApi implements InvocationHandler, ILuaObject {

    /**
     * Gets the names that the api can be called by in lua
     * @return The names
     */
    public abstract String[] getNames();

    /**
     * Called on computer startup
     */
    public abstract void startup();

    /**
     * Called to tick the API
     * @param dt (Probably) the computer date/time
     */
    public abstract void advance(double dt);

    /**
     * Called instead of @see LuaApi#advance(double) in CC:Tweaked
     */
    public abstract void update();

    /**
     * Called on computer shutdown
     */
    public abstract void shutdown();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        switch (method.getName()) {
            case "getNames":
                return getNames();
            case "startup":
                startup();
                break;
            case "advance":
                advance((Double) args[0]);
                break;
            case "update":
                update();
                break;
            case "shutdown":
                shutdown();
                break;
            case "getMethodNames":
                return getMethodNames();
            case "callMethod":
                return callMethod((ILuaContext) args[0], (Integer) args[1], (Object[]) args[2]);
        }
        return null;
    }
}
