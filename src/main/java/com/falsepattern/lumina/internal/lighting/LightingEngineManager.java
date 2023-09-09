/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.lighting;

import com.falsepattern.lumina.api.lighting.LumiLightingEngine;
import com.falsepattern.lumina.api.lighting.LumiLightingEngineProvider;
import com.falsepattern.lumina.api.lighting.LumiLightingEngineRegistry;
import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.internal.LumiDefaultValues;
import com.falsepattern.lumina.internal.event.EventPoster;
import lombok.NoArgsConstructor;
import lombok.val;
import net.minecraft.profiler.Profiler;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.falsepattern.lumina.internal.LUMINA.createLogger;
import static com.falsepattern.lumina.internal.lighting.NullLightingEngine.nullLightingEngine;
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
            LOG.error(new IllegalStateException("Cannot register lighting engine provider post registration"));
            return;
        }

        if (lightingEngineProvider == null) {
            LOG.error(new IllegalArgumentException("Lighting engine provider can't be null"));
            return;
        }

        val lightingEngineProviderID = lightingEngineProvider.lightingEngineProviderID();
        if (lightingEngineProviderID == null) {
            LOG.error(new IllegalArgumentException("Lighting engine provider id can't be null"));
            return;
        }

        if (lightingEngineProviderID.isEmpty()) {
            LOG.error(new IllegalArgumentException("Lighting engine provider id can't be empty"));
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
