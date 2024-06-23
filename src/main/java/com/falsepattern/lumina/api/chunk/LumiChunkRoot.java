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

package com.falsepattern.lumina.api.chunk;

import com.falsepattern.lib.StableAPI;
import net.minecraft.block.Block;
import org.jetbrains.annotations.NotNull;

import static com.falsepattern.lib.StableAPI.Expose;

@StableAPI(since = "__EXPERIMENTAL__")
public interface LumiChunkRoot {
    @Expose
    int BLOCK_LIGHT_ARRAY_SIZE = 16 * 16 * 16;
    @Expose
    int SKY_LIGHT_ARRAY_SIZE = 16 * 16 * 16;

    @Expose
    @NotNull
    String lumi$chunkRootID();

    @Expose
    boolean lumi$isUpdating();

    @Expose
    void lumi$markDirty();

    @Expose
    boolean lumi$isDirty();

    @Expose
    void lumi$prepareSubChunk(int chunkPosY);

    @Expose
    boolean lumi$isSubChunkPrepared(int chunkPosY);

    @Expose
    int lumi$topPreparedSubChunkBasePosY();

    @Expose
    @NotNull
    Block lumi$getBlock(int subChunkPosX, int posY, int subChunkPosZ);

    @Expose
    int lumi$getBlockMeta(int subChunkPosX, int posY, int subChunkPosZ);
}
