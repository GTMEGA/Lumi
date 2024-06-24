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

package com.falsepattern.lumi.internal.storage;

import com.falsepattern.chunk.api.DataManager;
import com.falsepattern.chunk.api.DataRegistry;
import com.falsepattern.lumi.api.chunk.LumiChunk;
import com.falsepattern.lumi.api.init.LumiChunkInitHook;
import com.falsepattern.lumi.api.lighting.LumiLightingEngine;
import com.falsepattern.lumi.internal.Tags;
import lombok.NoArgsConstructor;
import lombok.val;
import lombok.var;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static com.falsepattern.lumi.internal.Lumi.createLogger;
import static com.falsepattern.lumi.internal.Tags.MOD_ID;
import static com.falsepattern.lumi.internal.world.WorldProviderManager.worldProviderManager;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class ChunkNBTManager implements DataManager.ChunkDataManager {
    private static final Logger LOG = createLogger("Chunk NBT Manager");

    private static final ChunkNBTManager INSTANCE = new ChunkNBTManager();

    private boolean isRegistered = false;

    public static ChunkNBTManager chunkNBTManager() {
        return INSTANCE;
    }

    public void registerDataManager() {
        if (isRegistered)
            return;

        DataRegistry.registerDataManager(this);
        isRegistered = true;
        LOG.info("Registered data manager");
    }


    @Override
    public boolean chunkPrivilegedAccess() {
        return true;
    }

    @Override
    public String domain() {
        return MOD_ID;
    }

    @Override
    public String id() {
        return "lumi_chunk";
    }

    @Override
    public void writeChunkToNBT(Chunk chunkBase, NBTTagCompound output) {
        val worldBase = chunkBase.worldObj;
        val worldProviderManager = worldProviderManager();
        val worldProviderCount = worldProviderManager.worldProviderCount();
        boolean populated = true;
        int[] heightMap = null;
        for (var providerInternalID = 0; providerInternalID < worldProviderCount; providerInternalID++) {
            val worldProvider = worldProviderManager.getWorldProviderByInternalID(providerInternalID);
            if (worldProvider == null)
                continue;
            val world = worldProvider.provideWorld(worldBase);
            if (world == null)
                continue;
            val chunk = world.lumi$wrap(chunkBase);
            val lightingEngine = world.lumi$lightingEngine();

            val worldTag = Utils.writeWorldTag(output, world, worldProvider);

            writeChunkData(chunk, worldTag);
            writeLightingEngineData(chunk, lightingEngine, worldTag);

            populated &= chunk.lumi$isLightingInitialized();
            var currentHeightMap = chunk.lumi$skyLightHeightMap();
            if (currentHeightMap == null)
                continue;
            if (heightMap == null) {
                heightMap = Arrays.copyOf(currentHeightMap, currentHeightMap.length);
                continue;
            }

            int len = Math.min(heightMap.length, currentHeightMap.length);
            for (int i = 0; i < len; i++) {
                heightMap[i] = Math.min(heightMap[i], currentHeightMap[i]);
            }
        }
        output.setIntArray(LumiChunk.SKY_LIGHT_HEIGHT_MAP_NBT_TAG_NAME_VANILLA, heightMap);
        output.setBoolean(LumiChunk.IS_LIGHT_INITIALIZED_NBT_TAG_NAME_VANILLA, populated);
    }


    @Override
    public void readChunkFromNBT(Chunk chunkBase, NBTTagCompound input) {
        if (input.hasKey(domain())) {
            var domain = input.getCompoundTag(domain());
            if (domain.hasKey(id())) {
                readChunkFromNBTImpl(chunkBase, domain.getCompoundTag(id()), true);
                return;
            }
        }
        readChunkFromNBTImpl(chunkBase, input, false);
    }

    @Override
    public void cloneChunk(Chunk fromVanilla, Chunk toVanilla) {
        val worldBase = fromVanilla.worldObj;
        ensureInitialized(toVanilla, worldBase);
        val worldProviderManager = worldProviderManager();
        val worldProviderCount = worldProviderManager.worldProviderCount();
        for (var providerInternalID = 0; providerInternalID < worldProviderCount; providerInternalID++) {
            val worldProvider = worldProviderManager.getWorldProviderByInternalID(providerInternalID);
            if (worldProvider == null)
                continue;
            val world = worldProvider.provideWorld(worldBase);
            if (world == null)
                continue;
            val from = world.lumi$wrap(fromVanilla);
            val to = world.lumi$wrap(toVanilla);
            val lightingEngine = world.lumi$lightingEngine();

            cloneChunkData(from, to);
            cloneLightingEngineData(from, to, lightingEngine);
        }
    }

    private void ensureInitialized(Chunk toVanilla, World worldObj) {
        val toInit = (LumiChunkInitHook) toVanilla;
        if (toInit.lumi$initHookExecuted())
            return;
        boolean temporaryWorld = false;
        if (toVanilla.worldObj == null) {
            temporaryWorld = true;
            toVanilla.worldObj = worldObj;
        }
        toInit.lumi$doChunkInit();
        if (temporaryWorld) {
            toVanilla.worldObj = null;
        }
    }

    public void readChunkFromNBTImpl(Chunk chunkBase, NBTTagCompound input, boolean legacy) {
        val worldBase = chunkBase.worldObj;
        val worldProviderManager = worldProviderManager();
        val worldProviderCount = worldProviderManager.worldProviderCount();
        for (var providerInternalID = 0; providerInternalID < worldProviderCount; providerInternalID++) {
            val worldProvider = worldProviderManager.getWorldProviderByInternalID(providerInternalID);
            if (worldProvider == null)
                continue;
            val world = worldProvider.provideWorld(worldBase);
            if (world == null)
                continue;
            val chunk = world.lumi$wrap(chunkBase);
            val lightingEngine = world.lumi$lightingEngine();

            val worldTag = Utils.readWorldTag(input, world, worldProvider, legacy);
            if (worldTag == null) {
                initChunkData(chunk);
                initLightingEngineData(chunk, lightingEngine);
            } else {
                readChunkData(chunk, worldTag);
                readLightingEngineData(chunk, lightingEngine, worldTag);
            }
        }
    }

    private static void initChunkData(LumiChunk chunk) {
        val emptyTag = new NBTTagCompound();
        chunk.lumi$readFromNBT(emptyTag);
    }

    private static void initLightingEngineData(LumiChunk chunk, LumiLightingEngine lightingEngine) {
        val emptyTag = new NBTTagCompound();
        lightingEngine.readChunkFromNBT(chunk, emptyTag);
    }

    private static void writeChunkData(LumiChunk chunk, NBTTagCompound output) {
        val chunkTagName = chunk.lumi$chunkID();
        val chunkTag = new NBTTagCompound();
        chunk.lumi$writeToNBT(chunkTag);
        output.setTag(chunkTagName, chunkTag);
    }

    private static void writeLightingEngineData(LumiChunk chunk,
                                                LumiLightingEngine lightingEngine,
                                                NBTTagCompound output) {
        val lightingEngineTagName = lightingEngine.lightingEngineID();
        val lightingEngineTag = new NBTTagCompound();
        lightingEngine.writeChunkToNBT(chunk, lightingEngineTag);
        output.setTag(lightingEngineTagName, lightingEngineTag);
    }

    private static void readChunkData(LumiChunk chunk, NBTTagCompound input) {
        val chunkTagName = chunk.lumi$chunkID();
        if (input.hasKey(chunkTagName, 10)) {
            val chunkTag = input.getCompoundTag(chunkTagName);
            chunk.lumi$readFromNBT(chunkTag);
        }
    }

    private static void readLightingEngineData(LumiChunk chunk,
                                               LumiLightingEngine lightingEngine,
                                               NBTTagCompound input) {
        val lightingEngineTagName = lightingEngine.lightingEngineID();
        if (input.hasKey(lightingEngineTagName, 10)) {
            val lightingEngineTag = input.getCompoundTag(lightingEngineTagName);
            lightingEngine.readChunkFromNBT(chunk, lightingEngineTag);
        }
    }

    private static void cloneChunkData(LumiChunk from,
                                       LumiChunk to) {
        to.lumi$cloneFrom(from);
    }

    private static void cloneLightingEngineData(LumiChunk from,
                                                LumiChunk to,
                                                LumiLightingEngine lightingEngine) {
        lightingEngine.cloneChunk(from, to);
    }

    @Override
    public @NotNull String version() {
        return Tags.VERSION;
    }

    @Override
    public @Nullable String newInstallDescription() {
        return "Lumi chunk metadata. Compatible with vanilla saves.";
    }

    @Override
    public @NotNull String uninstallMessage() {
        return "Lumi chunk metadata. Fully compatible with vanilla, corruption very unlikely.";
    }

    @Override
    public @Nullable String versionChangeMessage(String priorVersion) {
        return null;
    }
}
