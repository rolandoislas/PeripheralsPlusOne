package com.austinv11.peripheralsplusplus.utils.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public abstract class Task implements InvocationHandler {

    /**
     * Get the owning Computer instance
     * @return Computer
     */
    public abstract Object getOwner();

    /**
     * Execute the task
     */
    public abstract void execute();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        switch (method.getName()) {
            case "getOwner":
                return getOwner();
            case "execute":
                execute();
        }
        return null;
    }
}
