/*
 * Lumi
 *
 * Copyright (C) 2023-2025 FalsePattern, Ven
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
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

package com.falsepattern.lumi.internal.lighting;

import com.falsepattern.lib.compat.BlockPos;
import com.falsepattern.lumi.api.chunk.LumiChunk;
import com.falsepattern.lumi.api.chunk.LumiSubChunk;
import com.falsepattern.lumi.api.lighting.LightType;
import com.falsepattern.lumi.api.lighting.LumiLightingEngine;
import lombok.NoArgsConstructor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class NullLightingEngine implements LumiLightingEngine {
    private static final NullLightingEngine INSTANCE = new NullLightingEngine();

    public static NullLightingEngine nullLightingEngine() {
        return INSTANCE;
    }

    @Override
    public @NotNull String lightingEngineID() {
        return "null_lighting_engine";
    }

    @Override
    public void writeChunkToNBT(@NotNull LumiChunk chunk, @NotNull NBTTagCompound output) {}

    @Override
    public void readChunkFromNBT(@NotNull LumiChunk chunk, @NotNull NBTTagCompound input) {}

    @Override
    public void cloneChunk(@NotNull LumiChunk from, @NotNull LumiChunk to) {}

    @Override
    public void writeSubChunkToNBT(@NotNull LumiChunk chunk,
                                   @NotNull LumiSubChunk subChunk,
                                   @NotNull NBTTagCompound output) {}

    @Override
    public void readSubChunkFromNBT(@NotNull LumiChunk chunk,
                                    @NotNull LumiSubChunk subChunk,
                                    @NotNull NBTTagCompound input) {}

    @Override
    public void cloneSubChunk(@NotNull LumiChunk fromChunk, @NotNull LumiSubChunk from, @NotNull LumiSubChunk to) {}

    @Override
    public void writeChunkToPacket(@NotNull LumiChunk chunk, @NotNull ByteBuffer output) {}

    @Override
    public void readChunkFromPacket(@NotNull LumiChunk chunk, @NotNull ByteBuffer input) {}

    @Override
    public void writeSubChunkToPacket(@NotNull LumiChunk chunk,
                                      @NotNull LumiSubChunk subChunk,
                                      @NotNull ByteBuffer input) {}

    @Override
    public void readSubChunkFromPacket(@NotNull LumiChunk chunk,
                                       @NotNull LumiSubChunk subChunk,
                                       @NotNull ByteBuffer output) {}

    @Override
    public int getCurrentLightValue(@NotNull LightType lightType, @NotNull BlockPos blockPos) {return 0;}

    @Override
    public int getCurrentLightValue(@NotNull LightType lightType, int posX, int posY, int posZ) {return 0;}

    @Override
    public int getCurrentLightValueChunk(@NotNull Chunk chunk, @NotNull LightType lightType, int chunkPosX, int posY, int chunkPosZ) {
        return 0;
    }

    @Override
    public boolean isChunkFullyLit(@NotNull LumiChunk chunk) {return false;}

    @Override
    public void handleChunkInit(@NotNull LumiChunk chunk) {}

    @Override
    public void handleClientChunkInit(@NotNull LumiChunk chunk) {}

    @Override
    public void handleSubChunkInit(@NotNull LumiChunk chunk, @NotNull LumiSubChunk subChunk) {}

    @Override
    public void handleChunkLoad(@NotNull LumiChunk chunk) {}

    @Override
    public void doRandomChunkLightingUpdates(@NotNull LumiChunk chunk) {}

    @Override
    public void updateLightingForBlock(@NotNull BlockPos blockPos) {}

    @Override
    public void updateLightingForBlock(int posX, int posY, int posZ) {}

    @Override
    public void scheduleLightingUpdateForRange(@NotNull LightType lightType, @NotNull BlockPos startBlockPos, @NotNull BlockPos endBlockPos) {}

    @Override
    public void scheduleLightingUpdateForRange(@NotNull LightType lightType, int startPosX, int startPosY, int startPosZ, int endPosX, int endPosY, int endPosZ) {}

    @Override
    public void scheduleLightingUpdateForColumn(@NotNull LightType lightType, int posX, int posZ) {}

    @Override
    public void scheduleLightingUpdateForColumn(@NotNull LightType lightType, int posX, int posZ, int startPosY, int endPosY) {}

    @Override
    public void scheduleLightingUpdate(@NotNull LightType lightType, @NotNull BlockPos blockPos) {}

    @Override
    public void scheduleLightingUpdate(@NotNull LightType lightType, int posX, int posY, int posZ) {}

    @Override
    public void processLightingUpdatesForType(@NotNull LightType lightType) {}

    @Override
    public void processLightingUpdatesForAllTypes() {}
}
