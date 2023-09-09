/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
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

    void writeChunkToNBT(@NotNull LumiChunk chunk, @NotNull NBTTagCompound output);

    void readChunkFromNBT(@NotNull LumiChunk chunk, @NotNull NBTTagCompound input);

    void writeSubChunkToNBT(@NotNull LumiChunk chunk,
                            @NotNull LumiSubChunk subChunk,
                            @NotNull NBTTagCompound output);

    void readSubChunkFromNBT(@NotNull LumiChunk chunk,
                             @NotNull LumiSubChunk subChunk,
                             @NotNull NBTTagCompound input);

    void writeChunkToPacket(@NotNull LumiChunk chunk, @NotNull ByteBuffer output);

    void readChunkFromPacket(@NotNull LumiChunk chunk, @NotNull ByteBuffer input);

    void writeSubChunkToPacket(@NotNull LumiChunk chunk,
                               @NotNull LumiSubChunk subChunk,
                               @NotNull ByteBuffer input);

    void readSubChunkFromPacket(@NotNull LumiChunk chunk,
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
