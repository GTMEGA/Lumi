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

package com.falsepattern.lumina.internal.data;

import com.falsepattern.chunk.api.ChunkDataManager;
import com.falsepattern.chunk.api.ChunkDataRegistry;
import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import com.falsepattern.lumina.api.lighting.LumiLightingEngine;
import com.falsepattern.lumina.internal.Tags;
import lombok.NoArgsConstructor;
import lombok.val;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.apache.logging.log4j.Logger;

import static com.falsepattern.lumina.api.LumiAPI.lumiWorldsFromBaseWorld;
import static com.falsepattern.lumina.internal.LUMINA.createLogger;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class SubChunkNBTManager implements ChunkDataManager.SectionNBTDataManager {
    private static final Logger LOG = createLogger("Sub Chunk NBT Manager");

    private static final String VERSION_NBT_TAG_NAME = Tags.MOD_ID + "_version";

    private static final String VERSION_NBT_TAG_VALUE = Tags.VERSION;

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
        output.setString(VERSION_NBT_TAG_NAME, VERSION_NBT_TAG_VALUE);
        val worldBase = chunkBase.worldObj;
        for (val world : lumiWorldsFromBaseWorld(worldBase)) {
            val subChunk = world.lumi$wrap(subChunkBase);
            val chunk = world.lumi$wrap(chunkBase);
            val lightingEngine = world.lumi$lightingEngine();

            val worldTagName = world.lumi$worldID();
            val worldTag = new NBTTagCompound();
            saveSubChunkData(subChunk, worldTag);
            saveLightingEngineData(chunk, subChunk, lightingEngine, worldTag);
            output.setTag(worldTagName, worldTag);
        }
    }

    @Override
    public void readSectionFromNBT(Chunk chunkBase, ExtendedBlockStorage subChunkBase, NBTTagCompound input) {
        val version = input.getString(VERSION_NBT_TAG_NAME);
        if (!VERSION_NBT_TAG_VALUE.equals(version))
            return;

        val worldBase = chunkBase.worldObj;
        for (val world : lumiWorldsFromBaseWorld(worldBase)) {
            val chunk = world.lumi$wrap(chunkBase);
            val subChunk = world.lumi$wrap(subChunkBase);
            val lightingEngine = world.lumi$lightingEngine();

            val worldTagName = world.lumi$worldID();
            if (!input.hasKey(worldTagName, 10))
                continue;
            val worldTag = input.getCompoundTag(worldTagName);
            readSubChunkData(subChunk, worldTag);
            readLightingEngineData(chunk, subChunk, lightingEngine, worldTag);
        }
    }

    private static void saveSubChunkData(LumiSubChunk subChunk, NBTTagCompound output) {
        val subChunkTagName = subChunk.lumi$subChunkID();
        val subChunkTag = new NBTTagCompound();
        subChunk.lumi$writeToNBT(subChunkTag);
        output.setTag(subChunkTagName, subChunkTag);
    }

    private static void saveLightingEngineData(LumiChunk chunk,
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
