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

package com.falsepattern.lumi.internal.lighting.phosphor;

import com.falsepattern.lumi.api.lighting.LumiLightingEngine;
import com.falsepattern.lumi.api.lighting.LumiLightingEngineProvider;
import com.falsepattern.lumi.api.world.LumiWorld;
import lombok.NoArgsConstructor;
import net.minecraft.profiler.Profiler;
import org.jetbrains.annotations.NotNull;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class PhosphorLightingEngineProvider implements LumiLightingEngineProvider {
    private static final PhosphorLightingEngineProvider INSTANCE = new PhosphorLightingEngineProvider();

    public static PhosphorLightingEngineProvider phosphorLightingEngineProvider() {
        return INSTANCE;
    }

    @Override
    public @NotNull String lightingEngineProviderID() {
        return "phosphor_lighting_engine_provider";
    }

    @Override
    public @NotNull LumiLightingEngine provideLightingEngine(@NotNull LumiWorld world, @NotNull Profiler profiler) {
        return new PhosphorLightingEngine(world, profiler);
    }
}
