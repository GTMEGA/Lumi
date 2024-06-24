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
import com.falsepattern.lumi.api.chunk.LumiSubChunk;
import com.falsepattern.lumi.internal.event.EventPoster;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.val;
import lombok.var;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

import static com.falsepattern.lumi.internal.Lumi.createLogger;
import static com.falsepattern.lumi.internal.Tags.MOD_ID;
import static com.falsepattern.lumi.internal.world.WorldProviderManager.worldProviderManager;
import static lombok.AccessLevel.PRIVATE;

@Accessors(fluent = true, chain = false)
@NoArgsConstructor(access = PRIVATE)
public final class ChunkPacketManager implements DataManager.PacketDataManager {
    private static final Logger LOG = createLogger("Chunk Packet Manager");

    private static final ChunkPacketManager INSTANCE = new ChunkPacketManager();

    private static final int BLOCKS_PER_SUB_CHUNK = 16 * 16 * 16;
    private static final int BITS_PER_BLOCK = 4 + 4;
    private static final int BYTES_PER_BLOCK = BITS_PER_BLOCK / 8;
    private static final int MAX_PACKET_SIZE_BYTES_PER_WORLD_PROVIDER = 1 + BLOCKS_PER_SUB_CHUNK * BYTES_PER_BLOCK;
    private static final int PROVIDER_ID_SIZE_BYTES = Integer.BYTES;
    private static final int PROVIDER_WRITTEN_BYTES_SIZE_BYTES = Integer.BYTES;
    private static final int HEADER_SIZE_BYTES = PROVIDER_ID_SIZE_BYTES + PROVIDER_WRITTEN_BYTES_SIZE_BYTES;

    @Getter
    private int maxPacketSize = 0;

    private boolean isRegistered = false;

    public static ChunkPacketManager chunkPacketManager() {
        return INSTANCE;
    }

    public void registerDataManager() {
        if (isRegistered)
            return;

        val worldProviderCount = worldProviderManager().worldProviderCount();
        val subChunkMaxPacketSize = MAX_PACKET_SIZE_BYTES_PER_WORLD_PROVIDER * worldProviderCount;
        maxPacketSize = EventPoster.postChunkPacketSizeEvent(0,
                                                             subChunkMaxPacketSize,
                                                             0);
        val maxHeaderSize = worldProviderCount * HEADER_SIZE_BYTES;
        maxPacketSize += maxHeaderSize;

        DataRegistry.registerDataManager(this);
        isRegistered = true;
        LOG.info("Registered data manager");
    }

    @Override
    public String domain() {
        return MOD_ID;
    }

    @Override
    public String id() {
        return "lumi_packet";
    }

    // What provider is the world from? (Providers have IDs assigned to them on registration)
    // What world has been provided?
    // How much data has been written? (Counting the written data is trivial)
    @Override
    public void writeToBuffer(Chunk chunkBase, int subChunkMask, boolean forceUpdate, ByteBuffer output) {
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
            output.putInt(providerInternalID);
            val lengthPosition = output.position();
            output.position(lengthPosition + PROVIDER_WRITTEN_BYTES_SIZE_BYTES);

            val chunk = world.lumi$wrap(chunkBase);
            val lightingEngine = world.lumi$lightingEngine();

            chunk.lumi$writeToPacket(output);
            for (var chunkPosY = 0; chunkPosY < 16; chunkPosY++) {
                val subChunk = getSubChunk(chunk, subChunkMask, chunkPosY);
                if (subChunk != null)
                    subChunk.lumi$writeToPacket(output);
            }
            lightingEngine.writeChunkToPacket(chunk, output);
            for (var chunkPosY = 0; chunkPosY < 16; chunkPosY++) {
                val subChunk = getSubChunk(chunk, subChunkMask, chunkPosY);
                if (subChunk != null)
                    lightingEngine.writeSubChunkToPacket(chunk, subChunk, output);
            }

            val length = output.position() - PROVIDER_WRITTEN_BYTES_SIZE_BYTES - lengthPosition;
            output.putInt(lengthPosition, length);
        }
    }

    @Override
    public void readFromBuffer(Chunk chunkBase, int subChunkMask, boolean forceUpdate, ByteBuffer input) {
        val worldBase = chunkBase.worldObj;
        val worldProviderManager = worldProviderManager();
        while (input.remaining() > 0) {
            val providerInternalID = input.getInt();
            val length = input.getInt();
            if (length == 0)
                continue;
            val startPosition = input.position();

            val worldProvider = worldProviderManager.getWorldProviderByInternalID(providerInternalID);
            if (worldProvider == null) {
                input.position(startPosition + length);
                continue;
            }
            val world = worldProvider.provideWorld(worldBase);
            if (world == null) {
                input.position(startPosition + length);
                continue;
            }

            val chunk = world.lumi$wrap(chunkBase);
            val lightingEngine = world.lumi$lightingEngine();

            chunk.lumi$readFromPacket(input);
            for (var chunkPosY = 0; chunkPosY < 16; chunkPosY++) {
                val subChunk = getSubChunk(chunk, subChunkMask, chunkPosY);
                if (subChunk != null)
                    subChunk.lumi$readFromPacket(input);
            }
            lightingEngine.readChunkFromPacket(chunk, input);
            for (var chunkPosY = 0; chunkPosY < 16; chunkPosY++) {
                val subChunk = getSubChunk(chunk, subChunkMask, chunkPosY);
                if (subChunk != null)
                    lightingEngine.readSubChunkFromPacket(chunk, subChunk, input);
            }

            input.position(startPosition + length);
        }
    }

    private static @Nullable LumiSubChunk getSubChunk(LumiChunk chunk, int subChunkMask, int chunkPosY) {
        if ((subChunkMask & (1 << chunkPosY)) == 0)
            return null;
        return chunk.lumi$getSubChunkIfPrepared(chunkPosY);
    }
}
