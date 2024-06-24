package com.falsepattern.lumi.internal.config;

import com.falsepattern.lib.config.ConfigException;
import com.falsepattern.lib.config.SimpleGuiConfig;
import com.falsepattern.lumi.internal.Tags;
import net.minecraft.client.gui.GuiScreen;

public final class LumiGuiConfig extends SimpleGuiConfig {
    public LumiGuiConfig(GuiScreen parent) throws ConfigException {
        super(parent, Tags.MOD_ID, Tags.MOD_NAME, LumiConfig.class);
    }
}
