/*
 * Lumi
 *
 * Copyright (C) 2023-2024 FalsePattern, Ven
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
 *
 */

package com.falsepattern.lumi.internal.lighting;

import com.falsepattern.lumi.api.lighting.LumiLightingEngine;
import com.falsepattern.lumi.api.lighting.LumiLightingEngineProvider;
import com.falsepattern.lumi.api.lighting.LumiLightingEngineRegistry;
import com.falsepattern.lumi.api.world.LumiWorld;
import com.falsepattern.lumi.internal.LumiDefaultValues;
import com.falsepattern.lumi.internal.event.EventPoster;
import lombok.NoArgsConstructor;
import lombok.val;
import net.minecraft.profiler.Profiler;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.falsepattern.lumi.internal.Lumi.createLogger;
import static com.falsepattern.lumi.internal.lighting.NullLightingEngine.nullLightingEngine;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class LightingEngineManager implements LumiLightingEngineRegistry, LumiLightingEngineProvider {
    private static final Logger LOG = createLogger("Lighting Provider Manager");

    private static final LightingEngineManager INSTANCE = new LightingEngineManager();

    private @Nullable LumiLightingEngineProvider delegate;

    private boolean isRegistered = false;

    public static LightingEngineManager lightingEngineManager() {
        return INSTANCE;
    }

    public void registerLightingEngineProvider() {
        if (isRegistered)
            return;

        EventPoster.postLumiLightingEngineRegistrationEvent(this);
        if (delegate == null)
            LumiDefaultValues.registerDefaultLightingEngineProvider(this);

        isRegistered = true;
    }

    @Override
    @SuppressWarnings("ConstantValue")
    public void registerLightingEngineProvider(@NotNull LumiLightingEngineProvider lightingEngineProvider,
                                               boolean displace) {
        if (isRegistered) {
            LOG.error("Cannot register lighting engine provider post registration", new IllegalStateException());
            return;
        }

        if (lightingEngineProvider == null) {
            LOG.error("Lighting engine provider can't be null", new IllegalArgumentException());
            return;
        }

        val lightingEngineProviderID = lightingEngineProvider.lightingEngineProviderID();
        if (lightingEngineProviderID == null) {
            LOG.error("Lighting engine provider id can't be null", new IllegalArgumentException());
            return;
        }

        if (lightingEngineProviderID.isEmpty()) {
            LOG.error("Lighting engine provider id can't be empty", new IllegalArgumentException());
            return;
        }

        if (delegate == null) {
            delegate = lightingEngineProvider;
            LOG.info("Registered lighting engine provider: [{}]", lightingEngineProviderID);
            return;
        }

        if (displace) {
            val oldLightingEngineProviderID = delegate.lightingEngineProviderID();
            LOG.warn("Lighting engine provider [{}] has been displaced with [{}], " +
                     "this may indicate a mod conflict but is probably fine.",
                     oldLightingEngineProviderID,
                     lightingEngineProviderID);
            delegate = lightingEngineProvider;
        }
    }

    @Override
    public @NotNull String lightingEngineProviderID() {
        if (delegate != null)
            return delegate.lightingEngineProviderID();
        return nullLightingEngine().lightingEngineID();
    }

    @Override
    public @NotNull LumiLightingEngine provideLightingEngine(@NotNull LumiWorld world, @NotNull Profiler profiler) {
        if (delegate != null)
            return delegate.provideLightingEngine(world, profiler);
        return nullLightingEngine();
    }
}
