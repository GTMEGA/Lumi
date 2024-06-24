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

package com.falsepattern.lumi.internal.lighting.phosphor;

import com.falsepattern.lumi.api.chunk.LumiChunk;
import com.falsepattern.lumi.api.world.LumiWorld;
import lombok.val;

import java.util.BitSet;

final class WorldChunkSlice {
    private static final int DIAMETER = 5;
    private static final int RADIUS = DIAMETER / 2;

    private final LumiWorld world;

    private final int minChunkPosX;
    private final int minChunkPosZ;

    private final BitSet checkedChunks;
    private final LumiChunk[] chunks;

    WorldChunkSlice(LumiWorld world, int chunkPosX, int chunkPosZ) {
        this.world = world;

        this.minChunkPosX = chunkPosX - RADIUS;
        this.minChunkPosZ = chunkPosZ - RADIUS;

        this.checkedChunks = new BitSet(DIAMETER * DIAMETER);
        this.chunks = new LumiChunk[DIAMETER * DIAMETER];
    }

    LumiChunk getChunkFromWorldCoords(int x, int z) {
        return this.getChunk((x >> 4) - this.minChunkPosX, (z >> 4) - this.minChunkPosZ);
    }

    boolean isLoaded(int x, int z, int radius) {
        return this.isLoaded(x - radius, z - radius, x + radius, z + radius);
    }

    boolean isLoaded(int xStart, int zStart, int xEnd, int zEnd) {
        xStart = (xStart >> 4) - this.minChunkPosX;
        zStart = (zStart >> 4) - this.minChunkPosZ;
        xEnd = (xEnd >> 4) - this.minChunkPosX;
        zEnd = (zEnd >> 4) - this.minChunkPosZ;

        for (int i = xStart; i <= xEnd; ++i) {
            for (int j = zStart; j <= zEnd; ++j) {
                if (this.getChunk(i, j) == null) {
                    return false;
                }
            }
        }

        return true;
    }

    private LumiChunk getChunk(int x, int z) {
        val index = (x * DIAMETER) + z;
        if (checkedChunks.get(index))
            return chunks[index];

        val chunk = world.lumi$getChunkFromChunkPosIfExists(minChunkPosX + x, minChunkPosZ + z);
        this.chunks[index] = chunk;
        checkedChunks.set(index);
        return chunk;
    }
}
