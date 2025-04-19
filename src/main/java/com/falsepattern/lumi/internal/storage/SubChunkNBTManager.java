/*
 * Lumi
 *
 * Copyright (C) 2023-2025 FalsePattern, Ven
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
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
import com.falsepattern.lumi.api.chunk.LumiSubChunk;
import com.falsepattern.lumi.api.init.LumiExtendedBlockStorageInitHook;
import com.falsepattern.lumi.api.lighting.LumiLightingEngine;
import com.falsepattern.lumi.internal.Tags;
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

import static com.falsepattern.lumi.internal.Lumi.createLogger;
import static com.falsepattern.lumi.internal.world.WorldProviderManager.worldProviderManager;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class SubChunkNBTManager implements DataManager.SubChunkDataManager {
    private static final Logger LOG = createLogger("Sub Chunk NBT Manager");

    private static final SubChunkNBTManager INSTANCE = new SubChunkNBTManager();

    private boolean isRegistered = false;

    public static SubChunkNBTManager subChunkNBTManager() {
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
    public boolean subChunkPrivilegedAccess() {
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
    public void writeSubChunkToNBT(Chunk chunkBase, ExtendedBlockStorage subChunkBase, NBTTagCompound output) {
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

        int length = accumulator.length;
        for (int i = 0; i < length; i++) {
            int aI = accumulator[i];
            int dI = data[i];
            accumulator[i] = (byte) ((Math.max((aI >>> 4) & 0xF, (dI >>> 4) & 0xF) << 4) |
                                     (Math.max( aI        & 0xF,  dI        & 0xF)     ));
        }
        return accumulator;
    }

    @Override
    public void readSubChunkFromNBT(Chunk chunkBase, ExtendedBlockStorage subChunkBase, NBTTagCompound input) {
        if (input.hasKey(domain())) {
            var domain = input.getCompoundTag(domain());
            if (domain.hasKey(id())) {
                readSubChunkFromNBTImpl(chunkBase, subChunkBase, domain.getCompoundTag(id()), true);
                return;
            }
        }
        readSubChunkFromNBTImpl(chunkBase, subChunkBase, input, false);
    }

    @Override
    public void cloneSubChunk(Chunk fromChunk, ExtendedBlockStorage fromVanilla, ExtendedBlockStorage toVanilla) {
        ensureInitialized(toVanilla);
        val worldBase = fromChunk.worldObj;
        val worldProviderManager = worldProviderManager();
        val worldProviderCount = worldProviderManager.worldProviderCount();
        for (var providerInternalID = 0; providerInternalID < worldProviderCount; providerInternalID++) {
            val worldProvider = worldProviderManager.getWorldProviderByInternalID(providerInternalID);
            if (worldProvider == null)
                continue;
            val world = worldProvider.provideWorld(worldBase);
            if (world == null)
                continue;
            val chunk = world.lumi$wrap(fromChunk);
            val from = world.lumi$wrap(fromVanilla);
            val to = world.lumi$wrap(toVanilla);
            val lightingEngine = world.lumi$lightingEngine();

            cloneSubChunkData(from, to);
            cloneLightingEngineData(chunk, from, to, lightingEngine);
        }
    }

    private void ensureInitialized(ExtendedBlockStorage toVanilla) {
        val toInit = (LumiExtendedBlockStorageInitHook) toVanilla;
        if (toInit.lumi$initHookExecuted())
            return;
        toInit.lumi$doExtendedBlockStorageInit();
    }

    public void readSubChunkFromNBTImpl(Chunk chunkBase, ExtendedBlockStorage subChunkBase, NBTTagCompound input, boolean legacy) {
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

    private static void cloneSubChunkData(LumiSubChunk from, LumiSubChunk to) {
        to.lumi$cloneFrom(from);
    }

    private static void cloneLightingEngineData(LumiChunk fromChunk,
                                                LumiSubChunk from,
                                                LumiSubChunk to,
                                                LumiLightingEngine lightingEngine) {
        lightingEngine.cloneSubChunk(fromChunk, from, to);
    }

    @Override
    public @NotNull String version() {
        return Tags.VERSION;
    }

    @Override
    public @Nullable String newInstallDescription() {
        return "Lumi chunk lighting data. Chunk lighting will be recomputed from scratch when loading old worlds.";
    }

    @Override
    public @NotNull String uninstallMessage() {
        return "Lumi chunk lighting data. Fully compatible with vanilla, corruption very unlikely.";
    }

    @Override
    public @Nullable String versionChangeMessage(String priorVersion) {
        return null;
    }
}
