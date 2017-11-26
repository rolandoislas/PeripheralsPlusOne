package com.austinv11.peripheralsplusplus.lua.api;

import com.austinv11.peripheralsplusplus.utils.proxy.LuaApi;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DynApi extends LuaApi {
    private final Object apiEnvironment;

    public DynApi(Object apiEnvironment) {
        this.apiEnvironment = apiEnvironment;
    }

    @Override
    public String[] getNames() {
        return new String[] {"dyn"};
    }

    @Override
    public void startup() {

    }

    @Override
    public void advance(double dt) {

    }

    @Override
    public void shutdown() {

    }

    @Nonnull
    @Override
    public String[] getMethodNames() {
        return new String[]{"parseVersion"};
    }

    @Nullable
    @Override
    public Object[] callMethod(@Nonnull ILuaContext context, int method, @Nonnull Object[] arguments)
            throws LuaException, InterruptedException {
        switch (method) {
            case 0:
                return parseVersion(arguments);
        }
        return new Object[0];
    }

    /**
     * Parse a string in the expected version format: major.minor.revision-release_type-release_version
     * @param arguments object array with the only entry being a string
     * @return object containing a map of the string converted to an integer table or nil
     * @throws LuaException arguments were incorrect
     */
    private Object[] parseVersion(Object[] arguments) throws LuaException {
        if (arguments.length != 1)
            throw new LuaException("Not enough arguments");
        if (!(arguments[0] instanceof String))
            throw new LuaException("Argument 1 expected to be a string");
        Pattern versionPattern = Pattern
                .compile("^(\\d+)?(?:\\.(\\d+))?(?:\\.(\\d+))?(?:-(alpha|beta|release))?(?:-(\\d+))?$");
        Matcher matcher = versionPattern.matcher((String)arguments[0]);
        if (!matcher.matches())
            return new Object[0];
        Map<Integer, Integer> versionMap = new HashMap<>();
        for (int groupIndex = 1; groupIndex <= matcher.groupCount(); groupIndex++) {
            String group = matcher.group(groupIndex);
            if (group == null)
                break;
            switch (group) {
                case "alpha":
                    versionMap.put(groupIndex, 0);
                    break;
                case "beta":
                    versionMap.put(groupIndex, 1);
                    break;
                case "release":
                    versionMap.put(groupIndex, 2);
                    break;
                default:
                    try {
                        versionMap.put(groupIndex, Integer.parseInt(group));
                    }
                    catch (NumberFormatException e) {
                        return new Object[0];
                    }
            }
        }
        for (int versionIndex = versionMap.size() + 1; versionIndex < 6; versionIndex++)
            versionMap.put(versionIndex, 0);
        return new Object[]{versionMap};
    }
}
