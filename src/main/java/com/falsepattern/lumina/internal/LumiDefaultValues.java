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

package com.falsepattern.lumina.internal;

import com.falsepattern.lumina.api.lighting.LumiLightingEngineRegistry;
import com.falsepattern.lumina.api.world.LumiWorldProviderRegistry;
import com.falsepattern.lumina.internal.world.DefaultWorldProvider;
import lombok.experimental.UtilityClass;

import static com.falsepattern.lumina.internal.lighting.phosphor.PhosphorLightingEngineProvider.phosphorLightingEngineProvider;
import static com.falsepattern.lumina.internal.world.DefaultWorldProvider.defaultWorldProvider;

@UtilityClass
public final class LumiDefaultValues {
    public static void registerDefaultWorldProvider(LumiWorldProviderRegistry registry) {
        registry.registerWorldProvider(defaultWorldProvider());
        DefaultWorldProvider.setRegistered();
    }

    public static void registerDefaultLightingEngineProvider(LumiLightingEngineRegistry registry) {
        registry.registerLightingEngineProvider(phosphorLightingEngineProvider(), false);
    }
}
