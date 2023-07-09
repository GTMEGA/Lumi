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
import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import com.falsepattern.lumina.api.world.LumiWorld;
import lombok.experimental.UtilityClass;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public final class LightingEngineHelpers {
    private static final Block DEFAULT_BLOCK = Blocks.air;
    private static final int DEFAULT_METADATA = 0;

    public static Block getBlockFromChunk(LumiChunk chunk, BlockPos blockPos) {
        val chunkPosY = blockPos.getY() / 16;
        val subChunk = chunk.lumi$subChunk(chunkPosY);
        return getBlockFromSubChunk(subChunk, blockPos);
    }

    public static Block getBlockFromSubChunk(LumiSubChunk subChunk, BlockPos blockPos) {
        if (subChunk == null)
            return DEFAULT_BLOCK;

        val subChunkPosX = blockPos.getX() & 15;
        val subChunkPosY = blockPos.getY() & 15;
        val subChunkPosZ = blockPos.getZ() & 15;
        return subChunk.lumi$root().lumi$getBlock(subChunkPosX, subChunkPosY, subChunkPosZ);
    }

    public static int getBlockMetaFromChunk(LumiChunk chunk, BlockPos blockPos) {
        val chunkPosY = blockPos.getY() / 16;
        val subChunk = chunk.lumi$subChunk(chunkPosY);
        return getBlockMetaFromSubChunk(subChunk, blockPos);
    }

    public static int getBlockMetaFromSubChunk(LumiSubChunk subChunk, BlockPos blockPos) {
        if (subChunk == null)
            return DEFAULT_METADATA;

        val subChunkPosX = blockPos.getX() & 15;
        val subChunkPosY = blockPos.getY() & 15;
        val subChunkPosZ = blockPos.getZ() & 15;
        return subChunk.lumi$root().lumi$getBlockMeta(subChunkPosX, subChunkPosY, subChunkPosZ);
    }

    public static @Nullable LumiChunk getLoadedChunk(LumiWorld world, int chunkPosX, int chunkPosZ) {
        val provider = world.lumi$root().lumi$chunkProvider();
        if (!provider.chunkExists(chunkPosX, chunkPosZ))
            return null;

        val baseChunk = provider.provideChunk(chunkPosX, chunkPosZ);
        return world.lumi$wrap(baseChunk);
    }
}
