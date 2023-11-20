/*
 * This file is part of LUMINA.
 *
 * LUMINA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LUMINA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LUMINA. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lumina.api.event;


import cpw.mods.fml.common.eventhandler.Event;

import static com.falsepattern.lumina.api.chunk.LumiChunk.SUB_CHUNK_ARRAY_SIZE;

@SuppressWarnings("unused")
public final class ChunkPacketSizeEvent extends Event {
    /**
     * Size limited to 1MiB
     */
    private static final int PACKET_SIZE_BYTES_LIMIT = 1024 * 1024;

    private int chunkMaxPacketSize;
    private int subChunkMaxPacketSize;
    private int lightingEngineMaxPacketSize;

    public ChunkPacketSizeEvent(int chunkMaxPacketSize, int subChunkMaxPacketSize, int lightingEngineMaxPacketSize) {
        this.chunkMaxPacketSize = chunkMaxPacketSize;
        this.subChunkMaxPacketSize = subChunkMaxPacketSize;
        this.lightingEngineMaxPacketSize = lightingEngineMaxPacketSize;
    }

    public void chunkMaxPacketSize(int chunkMaxPacketSize) {
        if (chunkMaxPacketSize <= 0)
            return;
        ensureValidPacketSize(chunkMaxPacketSize, subChunkMaxPacketSize, lightingEngineMaxPacketSize);
        this.chunkMaxPacketSize = Math.max(this.chunkMaxPacketSize, chunkMaxPacketSize);
    }

    public void lightingEngineMaxPacketSize(int lightingEngineMaxPacketSize) {
        if (lightingEngineMaxPacketSize <= 0)
            return;
        ensureValidPacketSize(chunkMaxPacketSize, chunkMaxPacketSize, lightingEngineMaxPacketSize);
        this.lightingEngineMaxPacketSize = Math.max(this.subChunkMaxPacketSize, lightingEngineMaxPacketSize);
    }

    public void subChunkMaxPacketSize(int subChunkMaxPacketSize) {
        if (subChunkMaxPacketSize <= 0)
            return;
        ensureValidPacketSize(chunkMaxPacketSize, subChunkMaxPacketSize, lightingEngineMaxPacketSize);
        this.subChunkMaxPacketSize = Math.max(this.subChunkMaxPacketSize, subChunkMaxPacketSize);
    }

    public int totalMaxPacketSize() {
        return chunkMaxPacketSize + (subChunkMaxPacketSize * SUB_CHUNK_ARRAY_SIZE) + lightingEngineMaxPacketSize;
    }

    private static void ensureValidPacketSize(int chunkMaxPacketSize,
                                              int subChunkMaxPacketSize,
                                              int lightingEngineMaxPacketSize) {
        if (chunkMaxPacketSize > PACKET_SIZE_BYTES_LIMIT)
            throw new IllegalArgumentException("Chunk packet max packet size cannot exceed 1 MiB");
        if (subChunkMaxPacketSize > PACKET_SIZE_BYTES_LIMIT)
            throw new IllegalArgumentException("Sub chunk packet max packet size cannot exceed 1 MiB");
        if (lightingEngineMaxPacketSize > PACKET_SIZE_BYTES_LIMIT)
            throw new IllegalArgumentException("Lighting engine max packet size cannot exceed 1 MiB");
        if ((chunkMaxPacketSize + subChunkMaxPacketSize + lightingEngineMaxPacketSize) > PACKET_SIZE_BYTES_LIMIT)
            throw new IllegalArgumentException("Total chunk data max packet size cannot exceed 1 MiB");
    }
}
