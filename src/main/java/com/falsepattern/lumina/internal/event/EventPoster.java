/*
 * This file is part of LUMINA.
 *
 * LUMINA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LUMINA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LUMINA. If not, see <https://www.gnu.org/licenses/>.
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
