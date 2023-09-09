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
import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import com.falsepattern.lumina.api.lighting.LumiLightingEngine;
import com.falsepattern.lumina.internal.Tags;
import lombok.NoArgsConstructor;
import lombok.val;
import lombok.var;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.apache.logging.log4j.Logger;

import static com.falsepattern.lumina.api.world.LumiWorldProvider.WORLD_PROVIDER_VERSION_NBT_TAG_NAME;
import static com.falsepattern.lumina.internal.LUMINA.createLogger;
import static com.falsepattern.lumina.internal.world.WorldProviderManager.worldProviderManager;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class SubChunkNBTManager implements ChunkDataManager.SectionNBTDataManager {
    private static final Logger LOG = createLogger("Sub Chunk NBT Manager");

    private static final SubChunkNBTManager INSTANCE = new SubChunkNBTManager();

    private boolean isRegistered = false;

    public static SubChunkNBTManager subChunkNBTManager() {
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
        return Tags.MOD_ID;
    }

    @Override
    public String id() {
        return "lumi_sub_chunk";
    }

    @Override
    public void writeSectionToNBT(Chunk chunkBase, ExtendedBlockStorage subChunkBase, NBTTagCompound output) {
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
            val subChunk = world.lumi$wrap(subChunkBase);
            val chunk = world.lumi$wrap(chunkBase);
            val lightingEngine = world.lumi$lightingEngine();

            val worldTagName = world.lumi$worldID();
            val worldTag = new NBTTagCompound();
            writeSubChunkData(subChunk, worldTag);
            writeLightingEngineData(chunk, subChunk, lightingEngine, worldTag);

            val worldProviderVersion = worldProvider.worldProviderVersion();
            worldTag.setString(WORLD_PROVIDER_VERSION_NBT_TAG_NAME, worldProviderVersion);
            output.setTag(worldTagName, worldTag);
        }
    }

    @Override
    public void readSectionFromNBT(Chunk chunkBase, ExtendedBlockStorage subChunkBase, NBTTagCompound input) {
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
            val subChunk = world.lumi$wrap(subChunkBase);
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

                readSubChunkData(subChunk, worldTag);
                readLightingEngineData(chunk, subChunk, lightingEngine, worldTag);
                continue;
            }

            initSubChunkData(subChunk);
            initLightingEngineData(chunk, subChunk, lightingEngine);
        }
    }

    private static void initSubChunkData(LumiSubChunk subChunk) {
        val emptyTag = new NBTTagCompound();
        subChunk.lumi$readFromNBT(emptyTag);
    }

    private static void initLightingEngineData(LumiChunk chunk,
                                               LumiSubChunk subChunk,
                                               LumiLightingEngine lightingEngine) {
        val emptyTag = new NBTTagCompound();
        lightingEngine.readSubChunkFromNBT(chunk, subChunk, emptyTag);
    }

    private static void writeSubChunkData(LumiSubChunk subChunk, NBTTagCompound output) {
        val subChunkTagName = subChunk.lumi$subChunkID();
        val subChunkTag = new NBTTagCompound();
        subChunk.lumi$writeToNBT(subChunkTag);
        output.setTag(subChunkTagName, subChunkTag);
    }

    private static void writeLightingEngineData(LumiChunk chunk,
                                                LumiSubChunk subChunk,
                                                LumiLightingEngine lightingEngine,
                                                NBTTagCompound worldTag) {
        val lightingEngineTagName = lightingEngine.lightingEngineID();
        val lightingEngineTag = new NBTTagCompound();
        lightingEngine.writeSubChunkToNBT(chunk, subChunk, lightingEngineTag);
        worldTag.setTag(lightingEngineTagName, lightingEngineTag);
    }

    private static void readSubChunkData(LumiSubChunk subChunk, NBTTagCompound input) {
        val subChunkTagName = subChunk.lumi$subChunkID();
        if (input.hasKey(subChunkTagName, 10)) {
            val subChunkTag = input.getCompoundTag(subChunkTagName);
            subChunk.lumi$readFromNBT(subChunkTag);
        }
    }

    private static void readLightingEngineData(LumiChunk chunk,
                                               LumiSubChunk subChunk,
                                               LumiLightingEngine lightingEngine,
                                               NBTTagCompound input) {
        val lightingEngineTagName = lightingEngine.lightingEngineID();
        if (input.hasKey(lightingEngineTagName, 10)) {
            val lightingEngineTag = input.getCompoundTag(lightingEngineTagName);
            lightingEngine.readSubChunkFromNBT(chunk, subChunk, lightingEngineTag);
        }
    }
}
