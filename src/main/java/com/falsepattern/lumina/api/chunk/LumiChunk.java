/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.api.chunk;

import com.falsepattern.lumina.api.lighting.LightType;
import com.falsepattern.lumina.api.world.LumiWorld;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

@SuppressWarnings("unused")
public interface LumiChunk {
    int SUB_CHUNK_ARRAY_SIZE = 16;
    int HEIGHT_MAP_ARRAY_SIZE = 16 * 16;
    int MAX_QUEUED_RANDOM_LIGHT_UPDATES = 16 * 16 * 16;

    String IS_LIGHT_INITIALIZED_NBT_TAG_NAME = "lighting_initialized";
    String SKY_LIGHT_HEIGHT_MAP_NBT_TAG_NAME = "sky_light_height_map";

    @NotNull LumiChunkRoot lumi$root();

    @NotNull LumiWorld lumi$world();

    @NotNull String lumi$chunkID();

    void lumi$writeToNBT(@NotNull NBTTagCompound output);

    void lumi$readFromNBT(@NotNull NBTTagCompound input);

    void lumi$writeToPacket(@NotNull ByteBuffer output);

    void lumi$readFromPacket(@NotNull ByteBuffer input);

    @Nullable LumiSubChunk lumi$getSubChunkIfPrepared(int chunkPosY);

    @NotNull LumiSubChunk lumi$getSubChunk(int chunkPosY);

    int lumi$chunkPosX();

    int lumi$chunkPosZ();

    void lumi$queuedRandomLightUpdates(int queuedRandomLightUpdates);

    int lumi$queuedRandomLightUpdates();

    void lumi$resetQueuedRandomLightUpdates();

    int lumi$getBrightness(@NotNull LightType lightType, int subChunkPosX, int posY, int subChunkPosZ);

    int lumi$getBrightness(int subChunkPosX, int posY, int subChunkPosZ);

    int lumi$getLightValue(int subChunkPosX, int posY, int subChunkPosZ);

    void lumi$setLightValue(@NotNull LightType lightType, int subChunkPosX, int posY, int subChunkPosZ, int lightValue);

    int lumi$getLightValue(@NotNull LightType lightType, int subChunkPosX, int posY, int subChunkPosZ);

    void lumi$setBlockLightValue(int subChunkPosX, int posY, int subChunkPosZ, int lightValue);

    int lumi$getBlockLightValue(int subChunkPosX, int posY, int subChunkPosZ);

    void lumi$setSkyLightValue(int subChunkPosX, int posY, int subChunkPosZ, int lightValue);

    int lumi$getSkyLightValue(int subChunkPosX, int posY, int subChunkPosZ);

    int lumi$getBlockBrightness(int subChunkPosX, int posY, int subChunkPosZ);

    int lumi$getBlockOpacity(int subChunkPosX, int posY, int subChunkPosZ);

    int lumi$getBlockBrightness(@NotNull Block block, int blockMeta, int subChunkPosX, int posY, int subChunkPosZ);

    int lumi$getBlockOpacity(@NotNull Block block, int blockMeta, int subChunkPosX, int posY, int subChunkPosZ);

    boolean lumi$canBlockSeeSky(int subChunkPosX, int posY, int subChunkPosZ);

    void lumi$skyLightHeight(int subChunkPosX, int subChunkPosZ, int skyLightHeight);

    int lumi$skyLightHeight(int subChunkPosX, int subChunkPosZ);

    void lumi$minSkyLightHeight(int minSkyLightHeight);

    int lumi$minSkyLightHeight();

    void lumi$resetSkyLightHeightMap();

    void lumi$isHeightOutdated(int subChunkPosX, int subChunkPosZ, boolean isHeightOutdated);

    boolean lumi$isHeightOutdated(int subChunkPosX, int subChunkPosZ);

    void lumi$resetOutdatedHeightFlags();

    void lumi$isLightingInitialized(boolean isLightingInitialized);

    boolean lumi$isLightingInitialized();

    void lumi$resetLighting();
}
