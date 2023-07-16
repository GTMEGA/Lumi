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
import com.falsepattern.lumina.internal.Tags;
import lombok.NoArgsConstructor;
import lombok.val;
import lombok.var;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.falsepattern.lumina.api.LumiAPI.lumiWorldsFromBaseWorld;
import static com.falsepattern.lumina.api.chunk.LumiChunk.HEIGHT_MAP_ARRAY_SIZE;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class ChunkNBTManager implements ChunkDataManager.ChunkNBTDataManager {
    private static final Logger LOG = LogManager.getLogger(Tags.MOD_NAME + "|Chunk NBT Manager");

    private static final ChunkNBTManager INSTANCE = new ChunkNBTManager();

    private static final String VERSION_NBT_TAG_NAME = Tags.MOD_ID + "_version";
    private static final String IS_LIGHT_INITIALIZED_NBT_TAG_NAME = "lighting_initialized";
    private static final String SKY_LIGHT_HEIGHT_MAP_NBT_TAG_NAME = "sky_light_height_map";

    private static final String VERSION_NBT_TAG_VALUE = Tags.VERSION;

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
        return Tags.MOD_ID;
    }

    @Override
    public String id() {
        return "lumi_chunk";
    }

    @Override
    public void writeChunkToNBT(Chunk chunkBase, NBTTagCompound output) {
        output.setString(VERSION_NBT_TAG_NAME, VERSION_NBT_TAG_VALUE);
        val skyLightHeightMap = new int[HEIGHT_MAP_ARRAY_SIZE];

        val worldBase = chunkBase.worldObj;
        for (val world : lumiWorldsFromBaseWorld(worldBase)) {
            val chunk = world.lumi$wrap(chunkBase);
            val lightingEngine = world.lumi$lightingEngine();

            val worldTagName = world.lumi$worldID();
            val worldTag = new NBTTagCompound();

            for (var subChunkPosZ = 0; subChunkPosZ < 16; subChunkPosZ++) {
                for (var subChunkPosX = 0; subChunkPosX < 16; subChunkPosX++) {
                    val index = (subChunkPosX + (subChunkPosZ * 16)) % 255;
                    val skyLightHeight = chunk.lumi$skyLightHeight(subChunkPosX, subChunkPosZ);
                    skyLightHeightMap[index] = skyLightHeight;
                }
            }
            worldTag.setIntArray(SKY_LIGHT_HEIGHT_MAP_NBT_TAG_NAME, skyLightHeightMap);
            worldTag.setBoolean(IS_LIGHT_INITIALIZED_NBT_TAG_NAME, chunk.lumi$isLightingInitialized());

            {
                val chunkTagName = chunk.lumi$chunkID();
                val chunkTag = new NBTTagCompound();
                chunk.lumi$writeToNBT(chunkTag);
                worldTag.setTag(chunkTagName, chunkTag);
            }

            {
                val lightingEngineTagName = lightingEngine.lightingEngineID();
                val lightingEngineTag = new NBTTagCompound();
                lightingEngine.lumi$writeToChunkNBT(chunk, lightingEngineTag);
                worldTag.setTag(lightingEngineTagName, lightingEngineTag);
            }

            output.setTag(worldTagName, worldTag);
        }
    }

    @Override
    public void readChunkFromNBT(Chunk chunkBase, NBTTagCompound input) {
        val version = input.getString(VERSION_NBT_TAG_NAME);
        if (!VERSION_NBT_TAG_VALUE.equals(version))
            return;

        val worldBase = chunkBase.worldObj;
        for (val world : lumiWorldsFromBaseWorld(worldBase)) {
            val chunk = world.lumi$wrap(chunkBase);
            val lightingEngine = world.lumi$lightingEngine();

            val worldTagName = world.lumi$worldID();
            if (!input.hasKey(worldTagName, 10))
                continue;
            val worldTag = input.getCompoundTag(worldTagName);

            var isLightingInitialized = false;
            skyLightHeightMapValidCheck:
            {
                if (!worldTag.hasKey(IS_LIGHT_INITIALIZED_NBT_TAG_NAME, 1))
                    break skyLightHeightMapValidCheck;
                val isLightInitialized = worldTag.getBoolean(IS_LIGHT_INITIALIZED_NBT_TAG_NAME);
                if (!isLightInitialized)
                    break skyLightHeightMapValidCheck;

                if (!worldTag.hasKey(SKY_LIGHT_HEIGHT_MAP_NBT_TAG_NAME, 11))
                    break skyLightHeightMapValidCheck;
                val skyLightHeightMap = worldTag.getIntArray(SKY_LIGHT_HEIGHT_MAP_NBT_TAG_NAME);
                if (skyLightHeightMap.length != HEIGHT_MAP_ARRAY_SIZE)
                    break skyLightHeightMapValidCheck;

                for (var subChunkPosZ = 0; subChunkPosZ < 16; subChunkPosZ++) {
                    for (var subChunkPosX = 0; subChunkPosX < 16; subChunkPosX++) {
                        val index = subChunkPosX + (subChunkPosZ * 16) % 255;
                        val skyLightHeight = skyLightHeightMap[index];
                        chunk.lumi$skyLightHeight(subChunkPosX, subChunkPosZ, skyLightHeight);
                    }
                }

                isLightingInitialized = true;
            }
            chunk.lumi$isLightingInitialized(isLightingInitialized);
            if (!isLightingInitialized)
                lightingEngine.handleChunkInit(chunk);

            val chunkTagName = chunk.lumi$chunkID();
            if (worldTag.hasKey(chunkTagName, 10)) {
                val chunkTag = worldTag.getCompoundTag(chunkTagName);
                chunk.lumi$readFromNBT(chunkTag);
            }

            val lightingEngineTagName = lightingEngine.lightingEngineID();
            if (worldTag.hasKey(lightingEngineTagName, 10)) {
                val lightingEngineTag = worldTag.getCompoundTag(lightingEngineTagName);
                lightingEngine.lumi$readFromChunkNBT(chunk, lightingEngineTag);
            }
        }
    }
}
