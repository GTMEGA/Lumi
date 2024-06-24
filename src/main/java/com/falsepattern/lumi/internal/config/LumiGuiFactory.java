package com.falsepattern.lumi.internal.config;

import com.falsepattern.lib.config.SimpleGuiFactory;
import lombok.NoArgsConstructor;
import net.minecraft.client.gui.GuiScreen;

@NoArgsConstructor
public final class LumiGuiFactory implements SimpleGuiFactory {
    static {
        LumiConfig.poke();
    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return LumiGuiConfig.class;
    }
}
