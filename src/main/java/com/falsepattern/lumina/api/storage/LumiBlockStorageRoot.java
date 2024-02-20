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

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;

import com.falsepattern.lumina.api.chunk.LumiChunkRoot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface LumiBlockStorageRoot {
    @NotNull String lumi$blockStorageRootID();

    boolean lumi$isClientSide();

    boolean lumi$hasSky();

    @NotNull Block lumi$getBlock(int posX, int posY, int posZ);

    int lumi$getBlockMeta(int posX, int posY, int posZ);

    boolean lumi$isAirBlock(int posX, int posY, int posZ);

    @Nullable TileEntity lumi$getTileEntity(int posX, int posY, int posZ);

    @Nullable LumiChunkRoot lumi$getChunkRootFromBlockPosIfExists(int posX, int posZ);

    @Nullable LumiChunkRoot lumi$getChunkRootFromChunkPosIfExists(int chunkPosX, int chunkPosZ);
}
