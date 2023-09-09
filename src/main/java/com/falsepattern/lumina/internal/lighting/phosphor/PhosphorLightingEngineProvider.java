/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.lighting.phosphor;

import com.falsepattern.lumina.api.lighting.LumiLightingEngine;
import com.falsepattern.lumina.api.lighting.LumiLightingEngineProvider;
import com.falsepattern.lumina.api.world.LumiWorld;
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
