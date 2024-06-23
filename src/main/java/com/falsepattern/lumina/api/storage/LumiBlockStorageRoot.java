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

package com.falsepattern.lumina.api.storage;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lumina.api.chunk.LumiChunkRoot;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.falsepattern.lib.StableAPI.Expose;

@StableAPI(since = "__EXPERIMENTAL__")
public interface LumiBlockStorageRoot {
    @Expose
    @NotNull
    String lumi$blockStorageRootID();

    @Expose
    boolean lumi$isClientSide();

    @Expose
    boolean lumi$hasSky();

    @Expose
    @NotNull
    Block lumi$getBlock(int posX, int posY, int posZ);

    @Expose
    int lumi$getBlockMeta(int posX, int posY, int posZ);

    @Expose
    boolean lumi$isAirBlock(int posX, int posY, int posZ);

    @Expose
    @Nullable
    TileEntity lumi$getTileEntity(int posX, int posY, int posZ);

    @Expose
    @Nullable
    LumiChunkRoot lumi$getChunkRootFromBlockPosIfExists(int posX, int posZ);

    @Expose
    @Nullable
    LumiChunkRoot lumi$getChunkRootFromChunkPosIfExists(int chunkPosX, int chunkPosZ);
}
