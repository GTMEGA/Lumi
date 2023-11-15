/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.storage;

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
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

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
    public boolean sectionPrivilegedAccess() {
        return true;
    }

    @Override
    public String domain() {
        return Tags.MOD_ID;
    }

    @Override
    public String id() {
        return "lumi_sub_chunk";
    }

    private static final byte[] EMPTY = new byte[2048];

    @Override
    public void writeSectionToNBT(Chunk chunkBase, ExtendedBlockStorage subChunkBase, NBTTagCompound output) {
        val worldBase = chunkBase.worldObj;
        val worldProviderManager = worldProviderManager();
        val worldProviderCount = worldProviderManager.worldProviderCount();
        byte[] blockLight = null;
        byte[] skyLight = null;
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

            val worldTag = Utils.writeWorldTag(output, world, worldProvider);
            writeSubChunkData(subChunk, worldTag);
            writeLightingEngineData(chunk, subChunk, lightingEngine, worldTag);

            blockLight = mixLights(blockLight, subChunk.lumi$getBlockLightArray());
            skyLight = mixLights(skyLight, subChunk.lumi$getSkyLightArray());
        }
        output.setByteArray(LumiSubChunk.BLOCK_LIGHT_NBT_TAG_NAME_VANILLA, blockLight == null ? EMPTY : blockLight);
        output.setByteArray(LumiSubChunk.SKY_LIGHT_NBT_TAG_NAME_VANILLA, skyLight == null ? EMPTY : skyLight);
    }

    private static byte[] mixLights(byte[] accumulator, NibbleArray input) {
        if (input == null)
            return accumulator;
        var data = input.data;
        if (accumulator == null)
            return Arrays.copyOf(data, data.length);

        for (int i = 0; i < accumulator.length; i++) {
            accumulator[i] = (byte) ((Math.max((accumulator[i] >>> 4) & 0xF, (data[i] >>> 4) & 0xF) << 4) |
                                     (Math.max( accumulator[i]        & 0xF,  data[i]        & 0xF)     ));
        }
        return accumulator;
    }

    @Override
    public void readSectionFromNBT(Chunk chunkBase, ExtendedBlockStorage subChunkBase, NBTTagCompound input) {
        if (input.hasKey(domain())) {
            var domain = input.getCompoundTag(domain());
            if (domain.hasKey(id())) {
                readSectionFromNBTImpl(chunkBase, subChunkBase, domain.getCompoundTag(id()), true);
                return;
            }
        }
        readSectionFromNBTImpl(chunkBase, subChunkBase, input, false);
    }

    public void readSectionFromNBTImpl(Chunk chunkBase, ExtendedBlockStorage subChunkBase, NBTTagCompound input, boolean legacy) {
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

            val worldTag = Utils.readWorldTag(input, world, worldProvider, legacy);
            if (worldTag == null) {
                initSubChunkData(subChunk);
                initLightingEngineData(chunk, subChunk, lightingEngine);
            } else {
                readSubChunkData(subChunk, worldTag);
                readLightingEngineData(chunk, subChunk, lightingEngine, worldTag);
            }
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

    @Override
    public @NotNull String version() {
        return Tags.VERSION;
    }

    @Override
    public @Nullable String newInstallDescription() {
        return "Lumina chunk lighting data. Chunk lighting will be recomputed from scratch when loading old worlds.";
    }

    @Override
    public @NotNull String uninstallMessage() {
        return "Lumina chunk lighting data. Fully compatible with vanilla, corruption very unlikely.";
    }

    @Override
    public @Nullable String versionChangeMessage(String priorVersion) {
        return null;
    }
}
