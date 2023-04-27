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

package com.falsepattern.lumina.internal.saving;

import com.falsepattern.chunk.api.ChunkDataManager;
import com.falsepattern.lumina.internal.Tags;
import com.falsepattern.lumina.internal.world.LumiWorldManager;
import com.falsepattern.lumina.internal.world.lighting.LightingHooks;
import lombok.val;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;

/**
 * IDK why index 0 needs to be done differently, but if I don't do this, we get chunk errors.
 * <p>
 * Uses direct casting to avoid the overhead of LumiWorldManager.
 */
public class LightChecksBuiltin implements ChunkDataManager.ChunkNBTDataManager{
    @Override
    public void writeChunkToNBT(Chunk chunk, NBTTagCompound nbt) {
        val lChunk = LumiWorldManager.getWorld(chunk.worldObj, 0).lumiWrap(chunk);
        LightingHooks.writeNeighborLightChecksToNBT(lChunk, nbt);
        nbt.setBoolean("LightPopulated", lChunk.lumiIsLightInitialized());
    }

    @Override
    public void readChunkFromNBT(Chunk chunk, NBTTagCompound nbt) {
        val lChunk = LumiWorldManager.getWorld(chunk.worldObj, 0).lumiWrap(chunk);
        LightingHooks.readNeighborLightChecksFromNBT(lChunk, nbt);
        lChunk.lumiIsLightInitialized(nbt.getBoolean("LightPopulated"));
    }

    @Override
    public String domain() {
        return Tags.MODNAME;
    }

    @Override
    public String id() {
        return "builtin";
    }

    @Override
    public boolean chunkPrivilegedAccess() {
        return true;
    }
}
