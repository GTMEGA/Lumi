/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.api.chunk;

import net.minecraft.block.Block;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface LumiChunkRoot {
    int BLOCK_LIGHT_ARRAY_SIZE = 16 * 16 * 16;
    int SKY_LIGHT_ARRAY_SIZE = 16 * 16 * 16;

    @NotNull String lumi$chunkRootID();

    boolean lumi$isUpdating();

    void lumi$markDirty();

    boolean lumi$isDirty();

    void lumi$prepareSubChunk(int chunkPosY);

    boolean lumi$isSubChunkPrepared(int chunkPosY);

    int lumi$topPreparedSubChunkBasePosY();

    @NotNull Block lumi$getBlock(int subChunkPosX, int posY, int subChunkPosZ);

    int lumi$getBlockMeta(int subChunkPosX, int posY, int subChunkPosZ);
}
