/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.api.world;

import com.falsepattern.lumina.api.chunk.LumiChunk;
import com.falsepattern.lumina.api.chunk.LumiSubChunk;
import com.falsepattern.lumina.api.lighting.LightType;
import com.falsepattern.lumina.api.lighting.LumiLightingEngine;
import com.falsepattern.lumina.api.storage.LumiBlockStorage;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface LumiWorld extends LumiBlockStorage {
    @NotNull LumiWorldRoot lumi$root();

    @NotNull String lumi$worldID();

    @NotNull LumiChunk lumi$wrap(@NotNull Chunk chunkBase);

    @NotNull LumiSubChunk lumi$wrap(@NotNull ExtendedBlockStorage subChunkBase);

    @Nullable LumiChunk lumi$getChunkFromBlockPosIfExists(int posX, int posZ);

    @Nullable LumiChunk lumi$getChunkFromChunkPosIfExists(int chunkPosX, int chunkPosZ);

    @NotNull LumiLightingEngine lumi$lightingEngine();

    void lumi$setLightValue(@NotNull LightType lightType, int posX, int posY, int posZ, int lightValue);

    void lumi$setBlockLightValue(int posX, int posY, int posZ, int lightValue);

    void lumi$setSkyLightValue(int posX, int posY, int posZ, int lightValue);
}
