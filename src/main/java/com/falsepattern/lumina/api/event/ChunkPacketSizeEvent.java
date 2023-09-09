/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
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
