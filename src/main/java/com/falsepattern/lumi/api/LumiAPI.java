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

package com.falsepattern.lumi.api;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lumi.api.lighting.LumiLightingEngine;
import com.falsepattern.lumi.api.lighting.LumiLightingEngineProvider;
import com.falsepattern.lumi.api.world.LumiWorld;
import net.minecraft.profiler.Profiler;
import org.jetbrains.annotations.NotNull;

import static com.falsepattern.lumi.internal.Tags.*;
import static com.falsepattern.lumi.internal.lighting.LightingEngineManager.lightingEngineManager;

@StableAPI(since = "1.0.0")
public final class LumiAPI {
    @StableAPI.Expose
    public static final String LUMI_MOD_ID = MOD_ID;
    @StableAPI.Expose
    public static final String LUMI_MOD_NAME = MOD_NAME;
    @StableAPI.Expose
    public static final String LUMI_VERSION = VERSION;

    private LumiAPI() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @StableAPI.Expose(since = "__EXPERIMENTAL__")
    public static @NotNull LumiLightingEngine provideLightingEngine(@NotNull LumiWorld world,
                                                                    @NotNull Profiler profiler) {
        return lightingEngineManager().provideLightingEngine(world, profiler);
    }

    @StableAPI.Expose(since = "__EXPERIMENTAL__")
    public static @NotNull LumiLightingEngineProvider lightingEngineProvider() {
        return lightingEngineManager();
    }
}
