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

package com.falsepattern.lumina.internal.world;

import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.api.world.LumiWorldProvider;
import com.falsepattern.lumina.api.world.LumiWorldRegistry;
import com.falsepattern.lumina.api.world.LumiWorldWrapper;
import com.falsepattern.lumina.internal.LumiDefaultValues;
import com.falsepattern.lumina.internal.Tags;
import com.falsepattern.lumina.internal.collection.WeakIdentityHashMap;
import com.falsepattern.lumina.internal.event.EventPoster;
import lombok.NoArgsConstructor;
import lombok.val;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor
public final class WorldManager implements LumiWorldRegistry, LumiWorldWrapper {
    private static final Logger LOG = LogManager.getLogger(Tags.MOD_NAME + "|World Provider Manager");

    private static final WorldManager INSTANCE = new WorldManager();

    private final Set<LumiWorldProvider> worldProviders = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<World, Iterable<LumiWorld>> providedWorlds = new WeakIdentityHashMap<>();
    private boolean hasRegistered = false;
    private boolean isHijacked = false;
    private @Nullable String hijackingMod = null;

    public static WorldManager worldManager() {
        return INSTANCE;
    }

    public void registerWorldProviders() {
        if (hasRegistered)
            return;

        EventPoster.postLumiWorldRegistrationEvent(this);
        if (isHijacked && worldProviders.isEmpty()) {
            LOG.error("Default world providers have been hijacked by {}, but it did not provide it's own replacements." +
                      " The hijacked state has been reset, but things may not work correctly. God speed.", hijackingMod);
            isHijacked = false;
        }

        if (!isHijacked)
            LumiDefaultValues.registerDefaultWorldProvider(this);

        hasRegistered = true;
        LOG.info("Registered [{}] world providers", worldProviders.size());
    }

    @Override
    @SuppressWarnings("ConstantValue")
    public void hijackDefaultWorldProviders(@NotNull String modName) {
        if (isHijacked) {
            LOG.warn("Default world providers has already been hijacked by: {}," +
                     " but {} has tried to hijack it again. Things will probably work fine.", hijackingMod, modName);
            return;
        }

        if (modName != null) {
            hijackingMod = modName;
            LOG.info("Default world providers have been hijacked by: {}", modName);
        } else {
            hijackingMod = "UNKNOWN MOD";
            val e = new IllegalArgumentException();
            LOG.error("A mod attempted to hijack the default world providers, but didn't provider did not name itself." +
                      " The hijack *was* performed, and things should be fine. But please report this.", e);
        }

        isHijacked = true;
    }

    @Override
    @SuppressWarnings("ConstantValue")
    public void registerWorldProvider(@NotNull LumiWorldProvider worldProvider) {
        if (worldProvider == null) {
            LOG.error("World provider can't be null", new IllegalArgumentException());
            return;
        }

        if (worldProviders.contains(worldProvider)) {
            LOG.error("World provider already registered", new IllegalArgumentException());
            return;
        }

        worldProviders.add(worldProvider);
    }


    @Override
    @SuppressWarnings("ConstantValue")
    public @NotNull @Unmodifiable Iterable<LumiWorld> lumiWorldsFromBaseWorld(@NotNull World baseWorld) {
        if (baseWorld == null || !hasRegistered)
            return Collections.emptyList();
        return providedWorlds.computeIfAbsent(baseWorld, this::collectProvidedWorlds);
    }

    private Iterable<LumiWorld> collectProvidedWorlds(World baseWorld) {
        val worldList = worldProviders.stream()
                                      .map(worldProvider -> worldProvider.provideWorld(baseWorld))
                                      .filter(Objects::nonNull)
                                      .collect(Collectors.toCollection(ArrayList::new));
        return Collections.unmodifiableList(worldList);
    }
}
