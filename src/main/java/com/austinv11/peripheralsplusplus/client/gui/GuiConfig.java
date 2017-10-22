package com.austinv11.peripheralsplusplus.client.gui;

import com.austinv11.collectiveframework.minecraft.config.ConfigException;
import com.austinv11.collectiveframework.minecraft.config.ConfigRegistry;
import com.austinv11.peripheralsplusplus.reference.Config;
import com.austinv11.peripheralsplusplus.reference.Reference;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.List;

public class GuiConfig extends net.minecraftforge.fml.client.config.GuiConfig {
    GuiConfig(GuiScreen parentScreen) {
        // FIXME TheFramework - com.austinv11.collectiveframework.minecraft.client.gui$GuiConfig#
        // GuiConfig(GuiScreen, String, String, Object) does not have a public constructor
        super(parentScreen,
                getCategories(Config.INSTANCE, Reference.MOD_ID),
                Reference.MOD_ID,
                false,
                false,
                Reference.MOD_NAME);
        titleLine2 = ConfigRegistry.getFilePath(Config.INSTANCE);
    }

    private static List<IConfigElement> getCategories(Object config, String modid) {
        try {
            return ConfigRegistry.getCategories(config, modid);
        } catch (ConfigException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
