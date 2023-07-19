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

package com.falsepattern.lumina.api.lighting;

import com.falsepattern.lib.compat.BlockPos;
import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

import static cpw.mods.fml.relauncher.Side.CLIENT;

@SuppressWarnings("unused")
public interface LumiLightingEngine {
    @NotNull String lightingEngineID();

    void lumi$writeChunkToNBT(@NotNull LumiChunk chunk, @NotNull NBTTagCompound output);

    void lumi$readChunkFromNBT(@NotNull LumiChunk chunk, @NotNull NBTTagCompound input);

    void lumi$writeSubChunkToNBT(@NotNull LumiChunk chunk,
                                 @NotNull LumiSubChunk subChunk,
                                 @NotNull NBTTagCompound output);

    void lumi$readSubChunkFromNBT(@NotNull LumiChunk chunk,
                                  @NotNull LumiSubChunk subChunk,
                                  @NotNull NBTTagCompound input);

    void lumi$writeChunkToPacket(@NotNull LumiChunk chunk, @NotNull ByteBuffer output);

    void lumi$readChunkFromPacket(@NotNull LumiChunk chunk, @NotNull ByteBuffer input);

    void lumi$writeSubChunkToPacket(@NotNull LumiChunk chunk,
                                    @NotNull LumiSubChunk subChunk,
                                    @NotNull ByteBuffer input);

    void lumi$readSubChunkFromPacket(@NotNull LumiChunk chunk,
                                     @NotNull LumiSubChunk subChunk,
                                     @NotNull ByteBuffer output);

    int getCurrentLightValue(@NotNull LightType lightType, @NotNull BlockPos blockPos);

    int getCurrentLightValue(@NotNull LightType lightType, int posX, int posY, int posZ);

    boolean isChunkFullyLit(@NotNull LumiChunk chunk);

    void handleChunkInit(@NotNull LumiChunk chunk);

    @SideOnly(CLIENT)
    void handleClientChunkInit(@NotNull LumiChunk chunk);

    void handleSubChunkInit(@NotNull LumiChunk chunk, @NotNull LumiSubChunk subChunk);

    void handleChunkLoad(@NotNull LumiChunk chunk);

    void doRandomChunkLightingUpdates(@NotNull LumiChunk chunk);

    void updateLightingForBlock(@NotNull BlockPos blockPos);

    void updateLightingForBlock(int posX, int posY, int posZ);

    void scheduleLightingUpdateForRange(@NotNull LightType lightType,
                                        @NotNull BlockPos startBlockPos,
                                        @NotNull BlockPos endBlockPos);

    void scheduleLightingUpdateForRange(@NotNull LightType lightType,
                                        int startPosX,
                                        int startPosY,
                                        int startPosZ,
                                        int endPosX,
                                        int endPosY,
                                        int endPosZ);

    void scheduleLightingUpdateForColumn(@NotNull LightType lightType, int posX, int posZ);

    void scheduleLightingUpdateForColumn(@NotNull LightType lightType, int posX, int posZ, int startPosY, int endPosY);

    void scheduleLightingUpdate(@NotNull LightType lightType, @NotNull BlockPos blockPos);

    void scheduleLightingUpdate(@NotNull LightType lightType, int posX, int posY, int posZ);

    void processLightingUpdatesForType(@NotNull LightType lightType);

    void processLightingUpdatesForAllTypes();
}
