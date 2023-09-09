/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal;

import com.falsepattern.lumina.api.lighting.LumiLightingEngineRegistry;
import com.falsepattern.lumina.api.world.LumiWorldProviderRegistry;
import lombok.experimental.UtilityClass;

import static com.falsepattern.lumina.internal.lighting.phosphor.PhosphorLightingEngineProvider.phosphorLightingEngineProvider;
import static com.falsepattern.lumina.internal.world.DefaultWorldProvider.defaultWorldProvider;

@UtilityClass
public final class LumiDefaultValues {
    public static void registerDefaultWorldProvider(LumiWorldProviderRegistry registry) {
        registry.registerWorldProvider(defaultWorldProvider());
    }

    public static void registerDefaultLightingEngineProvider(LumiLightingEngineRegistry registry) {
        registry.registerLightingEngineProvider(phosphorLightingEngineProvider(), false);
    }
}
