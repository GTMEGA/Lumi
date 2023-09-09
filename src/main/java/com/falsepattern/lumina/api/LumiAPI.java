/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.api;

import com.falsepattern.lumina.api.lighting.LumiLightingEngine;
import com.falsepattern.lumina.api.lighting.LumiLightingEngineProvider;
import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.api.world.LumiWorldWrapper;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static com.falsepattern.lumina.internal.Tags.*;
import static com.falsepattern.lumina.internal.lighting.LightingEngineManager.lightingEngineManager;
import static com.falsepattern.lumina.internal.world.WorldProviderManager.worldProviderManager;

@SuppressWarnings("unused")
public final class LumiAPI {
    public static final String LUMI_MOD_ID = MOD_ID;
    public static final String LUMI_MOD_NAME = MOD_NAME;
    public static final String LUMI_VERSION = VERSION;

    private static final LumiWorldWrapper WORLD_WRAPPER = worldProviderManager();
    private static final LumiLightingEngineProvider LIGHTING_ENGINE_PROVIDER = lightingEngineManager();

    private LumiAPI() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static @NotNull Iterable<LumiWorld> lumiWorldsFromBaseWorld(@NotNull World worldBase) {
        return WORLD_WRAPPER.lumiWorldsFromBaseWorld(worldBase);
    }

    public static @NotNull LumiLightingEngine provideLightingEngine(@NotNull LumiWorld world,
                                                                    @NotNull Profiler profiler) {
        return LIGHTING_ENGINE_PROVIDER.provideLightingEngine(world, profiler);
    }

    public static @NotNull LumiWorldWrapper worldWrapper() {
        return WORLD_WRAPPER;
    }

    public static @NotNull LumiLightingEngineProvider lightingEngineProvider() {
        return LIGHTING_ENGINE_PROVIDER;
    }
}
