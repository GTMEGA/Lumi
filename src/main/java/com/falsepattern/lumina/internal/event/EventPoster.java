/*
 * Copyright (c) 2023 FalsePattern, Ven
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lumina.internal.event;

import com.falsepattern.lumina.api.event.LumiLightingEngineRegistrationEvent;
import com.falsepattern.lumina.api.event.LumiWorldRegistrationEvent;
import com.falsepattern.lumina.api.lighting.LumiLightingEngineRegistry;
import com.falsepattern.lumina.api.world.LumiWorldRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventBus;
import lombok.experimental.UtilityClass;
import lombok.val;

@UtilityClass
public final class EventPoster {
    public static void postLumiWorldRegistrationEvent(LumiWorldRegistry registry) {
        val evt = new LumiWorldRegistrationEvent(registry);
        fmlCommonBus().post(evt);
    }

    public static void postLumiLightingEngineRegistrationEvent(LumiLightingEngineRegistry registry) {
        val evt = new LumiLightingEngineRegistrationEvent(registry);
        fmlCommonBus().post(evt);
    }

    private static EventBus fmlCommonBus() {
        return FMLCommonHandler.instance().bus();
    }
}
