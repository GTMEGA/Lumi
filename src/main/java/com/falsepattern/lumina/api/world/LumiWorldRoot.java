/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.api.world;

import com.falsepattern.lumina.api.cache.LumiBlockCacheRoot;
import com.falsepattern.lumina.api.chunk.LumiChunkRoot;
import com.falsepattern.lumina.api.storage.LumiBlockStorageRoot;
import net.minecraft.world.chunk.IChunkProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface LumiWorldRoot extends LumiBlockStorageRoot {
    @NotNull String lumi$worldRootID();

    void lumi$markBlockForRenderUpdate(int posX, int posY, int posZ);

    void lumi$scheduleLightingUpdate(int posX, int posY, int posZ);

    @NotNull IChunkProvider lumi$chunkProvider();

    boolean lumi$doChunksExistInRange(int minPosX, int minPosY, int minPosZ, int maxPosX, int maxPosY, int maxPosZ);

    boolean lumi$doChunksExistInRange(int centerPosX, int centerPosY, int centerPosZ, int blockRange);

    @Nullable LumiChunkRoot lumi$getChunkRootFromBlockPosIfExists(int posX, int posZ);

    @Nullable LumiChunkRoot lumi$getChunkRootFromChunkPosIfExists(int chunkPosX, int chunkPosZ);

    @NotNull LumiBlockCacheRoot lumi$blockCacheRoot();
}
