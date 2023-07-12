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

package com.falsepattern.lumina.internal.storage;

import com.falsepattern.chunk.api.ChunkDataManager;
import com.falsepattern.lumina.internal.Tags;
import com.falsepattern.lumina.internal.lighting.LightingHooksOld;
import com.falsepattern.lumina.internal.world.LumiWorldManager;
import lombok.NoArgsConstructor;
import lombok.val;
import lombok.var;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;

import java.nio.ByteBuffer;

@NoArgsConstructor
public final class LuminaDataManager implements ChunkDataManager.ChunkNBTDataManager,
                                                ChunkDataManager.PacketDataManager {
    @Override
    public void writeChunkToNBT(Chunk baseChunk, NBTTagCompound output) {
        output.setString(Tags.MODID + "_version", Tags.VERSION);
        for (var i = 0; i < LumiWorldManager.lumiWorldCount(); i++) {
            val world = LumiWorldManager.getWorld(baseChunk.worldObj, i);
            val chunk = world.lumi$wrap(baseChunk);
            val subTag = new NBTTagCompound();
            LightingHooksOld.writeNeighborLightChecksToNBT(chunk, subTag);

            subTag.setBoolean("LightPopulated", chunk.lumi$hasLightInitialized());
            subTag.setIntArray("HeightMap", chunk.lumi$skyLightHeights());
            output.setTag(world.lumi$worldID(), subTag);
        }
    }

    @Override
    public void readChunkFromNBT(Chunk baseChunk, NBTTagCompound input) {
        val version = input.getString(Tags.MODID + "_version");
        val forceRelight = !Tags.VERSION.equals(version);
        for (var i = 0; i < LumiWorldManager.lumiWorldCount(); i++) {
            val world = LumiWorldManager.getWorld(baseChunk.worldObj, i);
            val chunk = world.lumi$wrap(baseChunk);
            val subTag = input.getCompoundTag(world.lumi$worldID());
            LightingHooksOld.readNeighborLightChecksFromNBT(chunk, subTag);

            skyLightMapValidCheck:
            {
                if (forceRelight)
                    break skyLightMapValidCheck;
                if (!subTag.getBoolean("LightPopulated"))
                    break skyLightMapValidCheck;

                val heightMap = subTag.getIntArray("HeightMap");
                if (heightMap == null || heightMap.length != 256)
                    break skyLightMapValidCheck;

                System.arraycopy(heightMap, 0, chunk.lumi$skyLightHeights(), 0, 256);
                chunk.lumi$hasLightInitialized(true);
                continue;
            }

            chunk.lumi$hasLightInitialized(false);
            LightingHooksOld.generateSkylightMap(chunk);
        }
    }

    @Override
    public String domain() {
        return Tags.MODNAME;
    }

    @Override
    public String id() {
        return "extended";
    }

    @Override
    public int maxPacketSize() {
        return 0;
    }

    @Override
    public void writeToBuffer(Chunk baseChunk, int subChunkMask, boolean forceUpdate, ByteBuffer buffer) {
    }

    @Override
    public void readFromBuffer(Chunk baseChunk, int subChunkMask, boolean forceUpdate, ByteBuffer buffer) {
        val baseWorld = baseChunk.worldObj;
        val worldCount = LumiWorldManager.lumiWorldCount();
        for (var i = 0; i < worldCount; i++) {
            val world = LumiWorldManager.getWorld(baseWorld, i);
            val subChunk = world.lumi$wrap(baseChunk);
            subChunk.lumi$hasLightInitialized(true);
        }
    }
}
