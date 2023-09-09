/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.data;

import com.falsepattern.chunk.api.ChunkDataManager;
import com.falsepattern.chunk.api.ChunkDataRegistry;
import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.lighting.LumiLightingEngine;
import lombok.NoArgsConstructor;
import lombok.val;
import lombok.var;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.Logger;

import static com.falsepattern.lumina.api.world.LumiWorldProvider.WORLD_PROVIDER_VERSION_NBT_TAG_NAME;
import static com.falsepattern.lumina.internal.LUMINA.createLogger;
import static com.falsepattern.lumina.internal.Tags.MOD_ID;
import static com.falsepattern.lumina.internal.world.WorldProviderManager.worldProviderManager;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class ChunkNBTManager implements ChunkDataManager.ChunkNBTDataManager {
    private static final Logger LOG = createLogger("Chunk NBT Manager");

    private static final ChunkNBTManager INSTANCE = new ChunkNBTManager();

    private boolean isRegistered = false;

    public static ChunkNBTManager chunkNBTManager() {
        return INSTANCE;
    }

    public void registerDataManager() {
        if (isRegistered)
            return;

        ChunkDataRegistry.registerDataManager(this);
        isRegistered = true;
        LOG.info("Registered data manager");
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
        for (var providerInternalID = 0; providerInternalID < worldProviderCount; providerInternalID++) {
            val worldProvider = worldProviderManager.getWorldProviderByInternalID(providerInternalID);
            if (worldProvider == null)
                continue;
            val world = worldProvider.provideWorld(worldBase);
            if (world == null)
                continue;
            val chunk = world.lumi$wrap(chunkBase);
            val lightingEngine = world.lumi$lightingEngine();

            val worldTagName = world.lumi$worldID();
            val worldTag = new NBTTagCompound();
            writeChunkData(chunk, worldTag);
            writeLightingEngineData(chunk, lightingEngine, worldTag);

            val worldProviderVersion = worldProvider.worldProviderVersion();
            worldTag.setString(WORLD_PROVIDER_VERSION_NBT_TAG_NAME, worldProviderVersion);
            output.setTag(worldTagName, worldTag);
        }
    }

    @Override
    public void readChunkFromNBT(Chunk chunkBase, NBTTagCompound input) {
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

            tagCheck:
            {
                val worldTagName = world.lumi$worldID();
                if (!input.hasKey(worldTagName, 10))
                    break tagCheck;
                val worldTag = input.getCompoundTag(worldTagName);

                val worldProviderVersion = worldProvider.worldProviderVersion();
                if (!worldProviderVersion.equals(worldTag.getString(WORLD_PROVIDER_VERSION_NBT_TAG_NAME)))
                    break tagCheck;

                readChunkData(chunk, worldTag);
                readLightingEngineData(chunk, lightingEngine, worldTag);
                continue;
            }

            initChunkData(chunk);
            initLightingEngineData(chunk, lightingEngine);
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
}
