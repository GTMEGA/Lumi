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
import com.falsepattern.lumina.internal.lighting.LightingHooks;
import com.falsepattern.lumina.internal.lighting.phosphor.LightingHooksOld;
import lombok.NoArgsConstructor;
import lombok.val;
import lombok.var;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;

import static com.falsepattern.lumina.api.LumiAPI.lumiWorldsFromBaseWorld;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class ChunkLightingDataManager implements ChunkDataManager.ChunkNBTDataManager,
                                                       ChunkDataManager.PacketDataManager {
    private static final Logger LOG = LogManager.getLogger(Tags.MOD_NAME + "|Chunk Lighting Data Manager");

    private static final ChunkLightingDataManager INSTANCE = new ChunkLightingDataManager();

    private static final String VERSION_NBT_TAG_NAME = Tags.MOD_ID + "_version";
    private static final String LIGHT_INITIALIZED_NBT_TAG_NAME = "lighting_initialized";
    private static final String SKY_LIGHT_HEIGHT_MAP_NBT_TAG_NAME = "sky_light_height_map";

    private boolean isRegistered = false;

    public static ChunkLightingDataManager chunkDataManager() {
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
        return Tags.MOD_NAME;
    }

    @Override
    public String id() {
        return "lighting";
    }

    @Override
    public void writeChunkToNBT(Chunk baseChunk, NBTTagCompound output) {
        output.setString(VERSION_NBT_TAG_NAME, Tags.VERSION);

        val skyLightHeightMap = new int[256];
        for (val world : lumiWorldsFromBaseWorld(baseChunk.worldObj)) {
            val chunk = world.lumi$wrap(baseChunk);
            val subTag = new NBTTagCompound();

            // TODO: Make Lighting Engine handle this [4]
            LightingHooksOld.writeNeighborLightChecksToNBT(chunk, subTag);

            subTag.setBoolean(LIGHT_INITIALIZED_NBT_TAG_NAME, chunk.lumi$isLightingInitialized());
            for (var subChunkPosZ = 0; subChunkPosZ < 16; subChunkPosZ++) {
                for (var subChunkPosX = 0; subChunkPosX < 16; subChunkPosX++) {
                    val index = (subChunkPosX + (subChunkPosZ * 16)) % 255;
                    val skyLightHeight = chunk.lumi$skyLightHeight(subChunkPosX, subChunkPosZ);
                    skyLightHeightMap[index] = skyLightHeight;
                }
            }
            subTag.setIntArray(SKY_LIGHT_HEIGHT_MAP_NBT_TAG_NAME, skyLightHeightMap);
            output.setTag(world.lumi$worldID(), subTag);
        }
    }

    @Override
    public void readChunkFromNBT(Chunk baseChunk, NBTTagCompound input) {
        val version = input.getString(VERSION_NBT_TAG_NAME);
        val forceRelight = !Tags.VERSION.equals(version);

        for (val world : lumiWorldsFromBaseWorld(baseChunk.worldObj)) {
            val lightingEngine = world.lumi$lightingEngine();
            val chunk = world.lumi$wrap(baseChunk);
            val subTag = input.getCompoundTag(world.lumi$worldID());

            // TODO: Make Lighting Engine handle this [4]
            LightingHooksOld.readNeighborLightChecksFromNBT(chunk, subTag);

            skyLightHeightMapValidCheck:
            {
                if (forceRelight)
                    break skyLightHeightMapValidCheck;
                if (!subTag.getBoolean(LIGHT_INITIALIZED_NBT_TAG_NAME))
                    break skyLightHeightMapValidCheck;

                val skyLightHeights = subTag.getIntArray(SKY_LIGHT_HEIGHT_MAP_NBT_TAG_NAME);
                if (skyLightHeights == null || skyLightHeights.length != 256)
                    break skyLightHeightMapValidCheck;

                for (var subChunkPosZ = 0; subChunkPosZ < 16; subChunkPosZ++) {
                    for (var subChunkPosX = 0; subChunkPosX < 16; subChunkPosX++) {
                        val index = (subChunkPosX + (subChunkPosZ * 16)) % 255;
                        val skyLightHeight = skyLightHeights[index];
                        chunk.lumi$skyLightHeight(subChunkPosX, subChunkPosZ, skyLightHeight);
                    }
                }
                chunk.lumi$isLightingInitialized(true);
                continue;
            }

            chunk.lumi$isLightingInitialized(false);
            lightingEngine.handleChunkInit(chunk);
        }
    }

    @Override
    public int maxPacketSize() {
        return 0;
    }

    @Override
    public void writeToBuffer(Chunk baseChunk, int subChunkMask, boolean forceUpdate, ByteBuffer buffer) {
    }

    @Override
    public void readFromBuffer(Chunk baseChunk, int subChunkMask, boolean forceUpdate, ByteBuffer buffer) {
        LightingHooks.markClientChunkLightingInitialized(baseChunk);
    }
}
