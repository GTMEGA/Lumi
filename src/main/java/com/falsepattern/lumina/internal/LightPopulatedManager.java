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

package com.falsepattern.lumina.internal;

import com.falsepattern.chunk.api.ChunkDataManager;
import com.falsepattern.lumina.internal.world.lighting.LightingHooks;
import lombok.val;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;

public class LightPopulatedManager implements ChunkDataManager.ChunkNBTDataManager{
    @Override
    public void writeChunkToNBT(Chunk chunk, NBTTagCompound nbt) {
        for (int i = 0; i < LumiWorldManager.lumiWorldCount(); i++) {
            val lWorld = LumiWorldManager.getWorld(chunk.worldObj, i);
            val lChunk = lWorld.wrap(chunk);
            val subTag = new NBTTagCompound();
            LightingHooks.writeNeighborLightChecksToNBT(lChunk, subTag);

            subTag.setBoolean("LightPopulated", lChunk.isLightInitialized());
            nbt.setTag(lWorld.id(), subTag);
        }
    }

    @Override
    public void readChunkFromNBT(Chunk chunk, NBTTagCompound nbt) {
        if (nbt == null) {
            System.err.println("NO NBT AT " + chunk.xPosition + ", " + chunk.zPosition);
            return;
        };
        for (int i = 0; i < LumiWorldManager.lumiWorldCount(); i++) {
            val lWorld = LumiWorldManager.getWorld(chunk.worldObj, i);
            val lChunk = lWorld.wrap(chunk);
            val subTag = nbt.getCompoundTag(lWorld.id());
            LightingHooks.readNeighborLightChecksFromNBT(lChunk, subTag);
            lChunk.setLightInitialized(subTag.getBoolean("LightPopulated"));
        }
    }

    @Override
    public String domain() {
        return Tags.MODNAME;
    }

    @Override
    public String id() {
        return "lightPopulated";
    }
}
