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

package com.falsepattern.lumi.api.lighting;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.compat.BlockPos;
import com.falsepattern.lumi.api.chunk.LumiChunk;
import com.falsepattern.lumi.api.chunk.LumiSubChunk;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

import static com.falsepattern.lib.StableAPI.Expose;
import static cpw.mods.fml.relauncher.Side.CLIENT;

@StableAPI(since = "__EXPERIMENTAL__")
public interface LumiLightingEngine {
    @Expose
    @NotNull
    String lightingEngineID();

    @Expose
    void writeChunkToNBT(@NotNull LumiChunk chunk, @NotNull NBTTagCompound output);

    @Expose
    void readChunkFromNBT(@NotNull LumiChunk chunk, @NotNull NBTTagCompound input);

    @Expose
    void cloneChunk(@NotNull LumiChunk from, @NotNull LumiChunk to);

    @Expose
    void writeSubChunkToNBT(@NotNull LumiChunk chunk,
                            @NotNull LumiSubChunk subChunk,
                            @NotNull NBTTagCompound output);

    @Expose
    void readSubChunkFromNBT(@NotNull LumiChunk chunk,
                             @NotNull LumiSubChunk subChunk,
                             @NotNull NBTTagCompound input);

    @Expose
    void cloneSubChunk(@NotNull LumiChunk fromChunk,
                       @NotNull LumiSubChunk from,
                       @NotNull LumiSubChunk to);

    @Expose
    void writeChunkToPacket(@NotNull LumiChunk chunk, @NotNull ByteBuffer output);

    @Expose
    void readChunkFromPacket(@NotNull LumiChunk chunk, @NotNull ByteBuffer input);

    @Expose
    void writeSubChunkToPacket(@NotNull LumiChunk chunk,
                               @NotNull LumiSubChunk subChunk,
                               @NotNull ByteBuffer input);

    @Expose
    void readSubChunkFromPacket(@NotNull LumiChunk chunk,
                                @NotNull LumiSubChunk subChunk,
                                @NotNull ByteBuffer output);

    @Expose
    int getCurrentLightValue(@NotNull LightType lightType, @NotNull BlockPos blockPos);

    @Expose
    int getCurrentLightValue(@NotNull LightType lightType, int posX, int posY, int posZ);

    @Expose
    int getCurrentLightValueChunk(@NotNull Chunk chunk,
                                  @NotNull LightType lightType,
                                  int chunkPosX,
                                  int posY,
                                  int chunkPosZ);

    @Expose
    boolean isChunkFullyLit(@NotNull LumiChunk chunk);

    @Expose
    void handleChunkInit(@NotNull LumiChunk chunk);

    @Expose
    @SideOnly(CLIENT)
    void handleClientChunkInit(@NotNull LumiChunk chunk);

    @Expose
    void handleSubChunkInit(@NotNull LumiChunk chunk, @NotNull LumiSubChunk subChunk);

    @Expose
    void handleChunkLoad(@NotNull LumiChunk chunk);

    @Expose
    void doRandomChunkLightingUpdates(@NotNull LumiChunk chunk);

    @Expose
    void updateLightingForBlock(@NotNull BlockPos blockPos);

    @Expose
    void updateLightingForBlock(int posX, int posY, int posZ);

    @Expose
    void scheduleLightingUpdateForRange(@NotNull LightType lightType,
                                        @NotNull BlockPos startBlockPos,
                                        @NotNull BlockPos endBlockPos);

    @Expose
    void scheduleLightingUpdateForRange(@NotNull LightType lightType,
                                        int startPosX,
                                        int startPosY,
                                        int startPosZ,
                                        int endPosX,
                                        int endPosY,
                                        int endPosZ);

    @Expose
    void scheduleLightingUpdateForColumn(@NotNull LightType lightType, int posX, int posZ);

    @Expose
    void scheduleLightingUpdateForColumn(@NotNull LightType lightType, int posX, int posZ, int startPosY, int endPosY);

    @Expose
    void scheduleLightingUpdate(@NotNull LightType lightType, @NotNull BlockPos blockPos);

    @Expose
    void scheduleLightingUpdate(@NotNull LightType lightType, int posX, int posY, int posZ);

    @Expose
    void processLightingUpdatesForType(@NotNull LightType lightType);

    @Expose
    void processLightingUpdatesForAllTypes();
}
