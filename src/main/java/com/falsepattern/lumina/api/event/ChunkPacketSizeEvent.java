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

package com.falsepattern.lumina.api.event;


import cpw.mods.fml.common.eventhandler.Event;

import static com.falsepattern.lumina.api.chunk.LumiChunk.SUB_CHUNK_ARRAY_SIZE;

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
        ensureValidPacketSize(chunkMaxPacketSize, this.subChunkMaxPacketSize, this.lightingEngineMaxPacketSize);
        this.chunkMaxPacketSize = Math.max(this.chunkMaxPacketSize, chunkMaxPacketSize);
    }

    public void lightingEngineMaxPacketSize(int lightingEngineMaxPacketSize) {
        if (lightingEngineMaxPacketSize <= 0)
            return;
        ensureValidPacketSize(this.chunkMaxPacketSize, this.chunkMaxPacketSize, lightingEngineMaxPacketSize);
        this.lightingEngineMaxPacketSize = Math.max(this.subChunkMaxPacketSize, lightingEngineMaxPacketSize);
    }

    public void subChunkMaxPacketSize(int subChunkMaxPacketSize) {
        if (subChunkMaxPacketSize <= 0)
            return;
        ensureValidPacketSize(this.chunkMaxPacketSize, subChunkMaxPacketSize, this.lightingEngineMaxPacketSize);
        this.subChunkMaxPacketSize = Math.max(this.subChunkMaxPacketSize, subChunkMaxPacketSize);
    }

    public int totalMaxPacketSize() {
        return chunkMaxPacketSize + (subChunkMaxPacketSize * SUB_CHUNK_ARRAY_SIZE) + lightingEngineMaxPacketSize;
    }

    private static void ensureValidPacketSize(int chunkMaxPacketSize, int subChunkMaxPacketSize, int lightingEngineMaxPacketSize) {
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
