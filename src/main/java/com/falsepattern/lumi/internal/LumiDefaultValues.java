/*
 * Lumi
 *
 * Copyright (C) 2023-2025 FalsePattern, Ven
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.falsepattern.lumi.internal;

import com.falsepattern.lumi.api.lighting.LumiLightingEngineRegistry;
import com.falsepattern.lumi.api.world.LumiWorldProviderRegistry;
import com.falsepattern.lumi.internal.world.DefaultWorldProvider;
import lombok.experimental.UtilityClass;

import static com.falsepattern.lumi.internal.lighting.phosphor.PhosphorLightingEngineProvider.phosphorLightingEngineProvider;
import static com.falsepattern.lumi.internal.world.DefaultWorldProvider.defaultWorldProvider;

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
