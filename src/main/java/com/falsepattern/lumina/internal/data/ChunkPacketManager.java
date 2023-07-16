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
import com.falsepattern.lumina.internal.Tags;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.val;
import lombok.var;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

import static com.falsepattern.lumina.api.LumiAPI.lumiWorldsFromBaseWorld;
import static com.falsepattern.lumina.internal.world.WorldManager.worldManager;
import static lombok.AccessLevel.PRIVATE;

@Accessors(fluent = true, chain = false)
@NoArgsConstructor(access = PRIVATE)
public final class ChunkPacketManager implements ChunkDataManager.PacketDataManager {
    private static final Logger LOG = LogManager.getLogger(Tags.MOD_NAME + "|Chunk Packet Manager");

    private static final ChunkPacketManager INSTANCE = new ChunkPacketManager();

    private static final int BLOCKS_PER_CHUNK = 16 * 16 * 256;
    private static final int BITS_PER_BLOCK = 4 + 4;
    private static final int BYTES_PER_BLOCK = BITS_PER_BLOCK / 8;
    private static final int MAX_PACKET_SIZE_PER_WORLD_PROVIDER = BLOCKS_PER_CHUNK * BYTES_PER_BLOCK;

    @Getter
    private int maxPacketSize = 0;

    private boolean isRegistered = false;

    public static ChunkPacketManager chunkPacketManager() {
        return INSTANCE;
    }

    public void registerDataManager() {
        if (isRegistered)
            return;

        maxPacketSize = MAX_PACKET_SIZE_PER_WORLD_PROVIDER * worldManager().worldProviderCount();

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
        return "lumi_packet";
    }

    @Override
    public void writeToBuffer(Chunk chunkBase, int subChunkMask, boolean forceUpdate, ByteBuffer buffer) {
        val lightValuesData = new byte[2048];
        val lightValues = new NibbleArray(lightValuesData, 4);

        val worldBase = chunkBase.worldObj;
        for (val world : lumiWorldsFromBaseWorld(worldBase)) {
            val hasSky = world.lumi$root().lumi$hasSky();
            val chunk = world.lumi$wrap(chunkBase);
            for (var chunkPosY = 0; chunkPosY < 16; chunkPosY++) {
                val subChunk = getSubChunk(chunk, subChunkMask, chunkPosY);
                if (subChunk == null)
                    continue;

                for (var subChunkPosY = 0; subChunkPosY < 16; subChunkPosY++) {
                    for (var subChunkPosZ = 0; subChunkPosZ < 16; subChunkPosZ++) {
                        for (var subChunkPosX = 0; subChunkPosX < 16; subChunkPosX++) {
                            val lightValue = subChunk.lumi$getBlockLightValue(subChunkPosX, subChunkPosY, subChunkPosZ);
                            lightValues.set(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);
                        }
                    }
                }
                buffer.put(lightValuesData);

                if (!hasSky)
                    continue;

                for (var subChunkPosY = 0; subChunkPosY < 16; subChunkPosY++) {
                    for (var subChunkPosZ = 0; subChunkPosZ < 16; subChunkPosZ++) {
                        for (var subChunkPosX = 0; subChunkPosX < 16; subChunkPosX++) {
                            val lightValue = subChunk.lumi$getBlockLightValue(subChunkPosX, subChunkPosY, subChunkPosZ);
                            lightValues.set(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);
                        }
                    }
                }
                buffer.put(lightValuesData);
            }
        }
    }

    @Override
    public void readFromBuffer(Chunk chunkBase, int subChunkMask, boolean forceUpdate, ByteBuffer buffer) {
        val lightValuesData = new byte[2048];
        val lightValues = new NibbleArray(lightValuesData, 4);

        val worldBase = chunkBase.worldObj;
        for (val world : lumiWorldsFromBaseWorld(worldBase)) {
            val hasSky = world.lumi$root().lumi$hasSky();
            val chunk = world.lumi$wrap(chunkBase);
            for (var chunkPosY = 0; chunkPosY < 16; chunkPosY++) {
                val subChunk = getSubChunk(chunk, subChunkMask, chunkPosY);
                if (subChunk == null)
                    continue;

                buffer.get(lightValuesData);
                for (var subChunkPosY = 0; subChunkPosY < 16; subChunkPosY++) {
                    for (var subChunkPosZ = 0; subChunkPosZ < 16; subChunkPosZ++) {
                        for (var subChunkPosX = 0; subChunkPosX < 16; subChunkPosX++) {
                            val lightValue = lightValues.get(subChunkPosX, subChunkPosY, subChunkPosZ);
                            subChunk.lumi$setBlockLightValue(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);
                        }
                    }
                }

                if (!hasSky)
                    continue;

                buffer.get(lightValuesData);
                for (var subChunkPosY = 0; subChunkPosY < 16; subChunkPosY++) {
                    for (var subChunkPosZ = 0; subChunkPosZ < 16; subChunkPosZ++) {
                        for (var subChunkPosX = 0; subChunkPosX < 16; subChunkPosX++) {
                            val lightValue = lightValues.get(subChunkPosX, subChunkPosY, subChunkPosZ);
                            subChunk.lumi$setSkyLightValue(subChunkPosX, subChunkPosY, subChunkPosZ, lightValue);
                        }
                    }
                }
            }
        }
    }

    private static @Nullable LumiSubChunk getSubChunk(LumiChunk chunk, int subChunkMask, int chunkPosY) {
        if ((subChunkMask & (1 << chunkPosY)) == 0)
            return null;
        return chunk.lumi$getSubChunkIfPrepared(chunkPosY);
    }
}
