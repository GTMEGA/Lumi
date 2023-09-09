/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.api.chunk;

import com.falsepattern.lumina.api.lighting.LightType;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

@SuppressWarnings("unused")
public interface LumiSubChunk {
    String BLOCK_LIGHT_NBT_TAG_NAME = "block_light";
    String SKY_LIGHT_NBT_TAG_NAME = "sky_light";

    @NotNull LumiSubChunkRoot lumi$root();

    @NotNull String lumi$subChunkID();

    void lumi$writeToNBT(@NotNull NBTTagCompound output);

    void lumi$readFromNBT(@NotNull NBTTagCompound input);

    void lumi$writeToPacket(@NotNull ByteBuffer output);

    void lumi$readFromPacket(@NotNull ByteBuffer input);

    void lumi$setLightValue(@NotNull LightType lightType,
                            int subChunkPosX,
                            int subChunkPosY,
                            int subChunkPosZ,
                            int lightValue);

    int lumi$getLightValue(@NotNull LightType lightType,
                           int subChunkPosX,
                           int subChunkPosY,
                           int subChunkPosZ);

    void lumi$setBlockLightValue(int subChunkPosX, int subChunkPosY, int subChunkPosZ, int lightValue);

    int lumi$getBlockLightValue(int subChunkPosX, int subChunkPosY, int subChunkPosZ);

    void lumi$setSkyLightValue(int subChunkPosX, int subChunkPosY, int subChunkPosZ, int lightValue);

    int lumi$getSkyLightValue(int subChunkPosX, int subChunkPosY, int subChunkPosZ);
}
