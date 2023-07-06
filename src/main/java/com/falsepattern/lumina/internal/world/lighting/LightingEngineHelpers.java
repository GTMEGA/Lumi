/*
 * Copyright (C) 2023 FalsePattern
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

package com.falsepattern.lumina.internal.world.lighting;

import com.falsepattern.lib.compat.BlockPos;
import com.falsepattern.lumina.api.world.LumiWorld;
import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import lombok.val;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public class LightingEngineHelpers {
    private static final Block DEFAULT_BLOCK = Blocks.air;
    private static final int DEFAULT_METADATA = 0;

    // Avoids some additional logic in Chunk#getBlockState... 0 is always air
    static Block posToBlock(final BlockPos pos, final LumiChunk chunk) {
        return posToBlock(pos, chunk.subChunk(pos.getY() >> 4));
    }

    static Block posToBlock(final BlockPos pos, final LumiSubChunk section) {
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();

        if (section != null)
        {
            return section.subChunkRoot().getBlock(x & 15, y & 15, z & 15);
        }

        return DEFAULT_BLOCK;
    }

    // Avoids some additional logic in Chunk#getBlockState... 0 is always air
    static int posToMeta(final BlockPos pos, final LumiChunk chunk) {
        return posToMeta(pos, chunk.subChunk(pos.getY() >> 4));
    }

    static int posToMeta(final BlockPos pos, final LumiSubChunk section) {
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();

        if (section != null)
        {
            return section.subChunkRoot().getBlockMeta(x & 15, y & 15, z & 15);
        }

        return DEFAULT_METADATA;
    }


    public static LumiChunk getLoadedChunk(LumiWorld world, int chunkX, int chunkZ) {
        val provider = world.worldRoot().chunkProvider();
        if(!provider.chunkExists(chunkX, chunkZ))
            return null;
        return world.toLumiChunk(provider.provideChunk(chunkX, chunkZ));
    }
}
