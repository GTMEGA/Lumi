/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.event;

import com.falsepattern.lumina.api.event.ChunkPacketSizeEvent;
import com.falsepattern.lumina.api.event.LumiLightingEngineRegistrationEvent;
import com.falsepattern.lumina.api.event.LumiWorldProviderRegistrationEvent;
import com.falsepattern.lumina.api.lighting.LumiLightingEngineRegistry;
import com.falsepattern.lumina.api.world.LumiWorldProviderRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventBus;
import lombok.experimental.UtilityClass;
import lombok.val;

@UtilityClass
public final class EventPoster {
    private static final EventBus EVENT_BUS = FMLCommonHandler.instance().bus();

    public static void postLumiWorldProviderRegistrationEvent(LumiWorldProviderRegistry registry) {
        EVENT_BUS.post(new LumiWorldProviderRegistrationEvent(registry));
    }

    public static void postLumiLightingEngineRegistrationEvent(LumiLightingEngineRegistry registry) {
        EVENT_BUS.post(new LumiLightingEngineRegistrationEvent(registry));
    }

    public static int postChunkPacketSizeEvent(int chunkMaxPacketSize, int subChunkMaxPacketSize, int lightingEngineMaxPacketSize) {
        val evt = new ChunkPacketSizeEvent(chunkMaxPacketSize, subChunkMaxPacketSize, lightingEngineMaxPacketSize);
        EVENT_BUS.post(evt);
        return evt.totalMaxPacketSize();
    }
}
