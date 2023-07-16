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

package com.falsepattern.lumina.internal.lighting;

import com.falsepattern.lumina.api.lighting.LumiLightingEngine;
import com.falsepattern.lumina.api.lighting.LumiLightingEngineProvider;
import com.falsepattern.lumina.api.lighting.LumiLightingEngineRegistry;
import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.internal.LumiDefaultValues;
import com.falsepattern.lumina.internal.Tags;
import com.falsepattern.lumina.internal.event.EventPoster;
import lombok.NoArgsConstructor;
import net.minecraft.profiler.Profiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class LightingEngineManager implements LumiLightingEngineRegistry, LumiLightingEngineProvider {
    private static final Logger LOG = LogManager.getLogger(Tags.MOD_NAME + "|World Provider Manager");

    private static final LightingEngineManager INSTANCE = new LightingEngineManager();

    private LumiLightingEngineProvider delegate;

    private boolean hasRegistered = false;

    public static LightingEngineManager lightingEngineManager() {
        return INSTANCE;
    }

    public void registerLightingEngineProvider() {
        if (hasRegistered)
            return;

        EventPoster.postLumiLightingEngineRegistrationEvent(this);
        if (delegate == null)
            LumiDefaultValues.registerDefaultLightingEngineProvider(this);

        hasRegistered = true;
        LOG.info("Registered lighting engine provider");
    }

    @Override
    @SuppressWarnings("ConstantValue")
    public void registerLightingEngineProvider(@NotNull LumiLightingEngineProvider lightingEngineProvider,
                                               boolean displace) {
        if (lightingEngineProvider == null) {
            LOG.error("Lighting engine provider can't be null", new IllegalArgumentException());
            return;
        }

        if (delegate == null) {
            delegate = lightingEngineProvider;
            return;
        }

        if (displace) {
            LOG.warn("Lighting engine has been displaced, this may indicate a mod conflict but is probably fine.");
            delegate = lightingEngineProvider;
        }
    }

    @Override
    public LumiLightingEngine provideLightingEngine(@NotNull LumiWorld world, @NotNull Profiler profiler) {
        return delegate.provideLightingEngine(world, profiler);
    }
}