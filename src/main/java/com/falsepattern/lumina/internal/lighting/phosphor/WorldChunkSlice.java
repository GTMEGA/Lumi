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

package com.falsepattern.lumina.internal.lighting.phosphor;

import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.world.LumiWorld;

import static com.falsepattern.lumina.internal.lighting.phosphor.LightingHooksOld.getLoadedChunk;

final class WorldChunkSlice {
    private static final int DIAMETER = 5;

    private final LumiChunk[] chunks;

    private final int x, z;

    WorldChunkSlice(LumiWorld world, int x, int z) {
        this.chunks = new LumiChunk[DIAMETER * DIAMETER];

        int radius = DIAMETER / 2;

        for (int xDiff = -radius; xDiff <= radius; xDiff++) {
            for (int zDiff = -radius; zDiff <= radius; zDiff++) {
                LumiChunk chunk = getLoadedChunk(world, x + xDiff, z + zDiff);
                this.chunks[((xDiff + radius) * DIAMETER) + (zDiff + radius)] = chunk;
            }
        }

        this.x = x - radius;
        this.z = z - radius;
    }

    LumiChunk getChunk(int x, int z) {
        return this.chunks[(x * DIAMETER) + z];
    }

    LumiChunk getChunkFromWorldCoords(int x, int z) {
        return this.getChunk((x >> 4) - this.x, (z >> 4) - this.z);
    }

    boolean isLoaded(int x, int z, int radius) {
        return this.isLoaded(x - radius, z - radius, x + radius, z + radius);
    }

    boolean isLoaded(int xStart, int zStart, int xEnd, int zEnd) {
        xStart = (xStart >> 4) - this.x;
        zStart = (zStart >> 4) - this.z;
        xEnd = (xEnd >> 4) - this.x;
        zEnd = (zEnd >> 4) - this.z;

        for (int i = xStart; i <= xEnd; ++i) {
            for (int j = zStart; j <= zEnd; ++j) {
                if (this.getChunk(i, j) == null) {
                    return false;
                }
            }
        }

        return true;
    }
}
