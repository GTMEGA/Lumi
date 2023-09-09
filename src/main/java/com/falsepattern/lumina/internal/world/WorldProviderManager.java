/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.world;

import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.api.world.LumiWorldProvider;
import com.falsepattern.lumina.api.world.LumiWorldProviderRegistry;
import com.falsepattern.lumina.api.world.LumiWorldWrapper;
import com.falsepattern.lumina.internal.LumiDefaultValues;
import com.falsepattern.lumina.internal.collection.WeakIdentityHashMap;
import com.falsepattern.lumina.internal.event.EventPoster;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.val;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.stream.Collectors;

import static com.falsepattern.lumina.internal.LUMINA.createLogger;
import static lombok.AccessLevel.PRIVATE;

@Accessors(fluent = true, chain = false)
@NoArgsConstructor(access = PRIVATE)
public final class WorldProviderManager implements LumiWorldProviderRegistry, LumiWorldWrapper {
    private static final Logger LOG = createLogger("World Provider Manager");

    private static final WorldProviderManager INSTANCE = new WorldProviderManager();

    private final List<LumiWorldProvider> worldProviders = new ArrayList<>();
    private final Map<World, Iterable<LumiWorld>> providedWorlds = new WeakIdentityHashMap<>();

    private boolean isRegistered = false;
    private boolean isHijacked = false;
    private @Nullable String hijackingMod = null;

    public static WorldProviderManager worldProviderManager() {
        return INSTANCE;
    }

    public void registerWorldProviders() {
        if (isRegistered)
            return;

        EventPoster.postLumiWorldProviderRegistrationEvent(this);
        if (isHijacked && worldProviders.isEmpty()) {
            LOG.error("Default world providers have been hijacked by [{}], " +
                      "but it did not provide it's own replacements. " +
                      "The hijacked state has been reset, but things may not work correctly. God speed.",
                      hijackingMod);
            isHijacked = false;
        }

        if (!isHijacked)
            LumiDefaultValues.registerDefaultWorldProvider(this);

        isRegistered = true;
    }

    @Override
    @SuppressWarnings("ConstantValue")
    public void hijackDefaultWorldProviders(@NotNull String modName) {
        if (isRegistered) {
            LOG.error("Cannot hijack default world providers post registration", new IllegalStateException());
            return;
        }

        if (isHijacked) {
            LOG.warn("Default world providers has already been hijacked by: [{}]," +
                     " but {} has tried to hijack it again. Things will probably work fine.", hijackingMod, modName);
            return;
        }

        if (modName != null) {
            hijackingMod = modName;
            LOG.info("Default world providers have been hijacked by: [{}]", modName);
        } else {
            hijackingMod = "UNKNOWN MOD";
            LOG.error("A mod attempted to hijack the default world providers, " +
                      "but didn't provider did not name itself. " +
                      "The hijack *was* performed, and things should be fine. But please report this.",
                      new IllegalArgumentException());
        }

        isHijacked = true;
    }

    @Override
    @SuppressWarnings("ConstantValue")
    public void registerWorldProvider(@NotNull LumiWorldProvider worldProvider) {
        if (isRegistered) {
            LOG.error(new IllegalStateException("Cannot registration world providers post registration"));
            return;
        }

        if (worldProvider == null) {
            LOG.error(new IllegalArgumentException("World provider can't be null"));
            return;
        }

        val worldProviderID = worldProvider.worldProviderID();
        if (worldProviderID == null) {
            LOG.error(new IllegalArgumentException("World provider id can't be null"));
            return;
        }

        if (worldProviderID.isEmpty()) {
            LOG.error(new IllegalArgumentException("World provider id can't be empty"));
            return;
        }

        if (worldProviders.contains(worldProvider)) {
            LOG.error(new IllegalArgumentException(
                    String.format("World provider [%s] already registered", worldProviderID)));
            return;
        }

        LOG.info("Registered world provider: [{}]", worldProviderID);
        worldProviders.add(worldProvider);
    }

    @Override
    public @NotNull @Unmodifiable Iterable<LumiWorld> lumiWorldsFromBaseWorld(@Nullable World worldBase) {
        if (!isRegistered) {
            LOG.error(new IllegalStateException("No world providers exist during registration, " +
                                                "an empty iterable will be returned."));
            return Collections.emptyList();
        }
        if (worldBase == null)
            return Collections.emptyList();
        return providedWorlds.computeIfAbsent(worldBase, this::collectProvidedWorlds);
    }

    public @Nullable LumiWorldProvider getWorldProviderByInternalID(int internalWorldProviderID) {
        if (internalWorldProviderID >= 0 && internalWorldProviderID < worldProviders.size())
            return worldProviders.get(internalWorldProviderID);
        return null;
    }

    public int worldProviderCount() {
        return worldProviders.size();
    }

    private Iterable<LumiWorld> collectProvidedWorlds(World worldBase) {
        val worldList = worldProviders.stream()
                                      .map(worldProvider -> worldProvider.provideWorld(worldBase))
                                      .filter(Objects::nonNull)
                                      .collect(Collectors.toCollection(ArrayList::new));
        return Collections.unmodifiableList(worldList);
    }
}
